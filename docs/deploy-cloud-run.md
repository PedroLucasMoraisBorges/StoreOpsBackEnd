# Deploy do StoreOps Backend no Google Cloud Run

Este documento descreve o processo completo de configuração e deploy da API StoreOps no Google Cloud Run com Cloud SQL (PostgreSQL).

---

## Pré-requisitos

- Conta no Google Cloud com projeto criado
- `gcloud` CLI instalado e autenticado
- Repositório no GitHub com o código do backend

---

## 1. Cloud SQL — Criação do Banco de Dados

### 1.1 Criar instância PostgreSQL

No console do Google Cloud, acesse **Cloud SQL → Criar instância → PostgreSQL**.

Configurações recomendadas:
- **Versão**: PostgreSQL 18
- **ID da instância**: `storeops-db` (ou outro nome de sua escolha)
- **Região**: a mesma do Cloud Run (ex: `southamerica-east1`)
- **Senha do usuário `postgres`**: defina uma senha forte

### 1.2 Criar o banco de dados

Após criar a instância, acesse **Bancos de dados → Criar banco de dados** e crie um banco com o nome:

```
StoreOpsDB
```

### 1.3 Criar usuário de aplicação

Acesse **Usuários → Adicionar conta de usuário** e crie:

- **Usuário**: `StoreOpsUser` (ou o nome que preferir)
- **Senha**: defina uma senha forte — anote, pois será usada no Secret Manager

### 1.4 Anotar o Connection Name

Na página da instância, copie o **Nome de conexão** (formato `projeto:região:instância`). Exemplo:

```
projeto-teste-reddactor:southamerica-east1:storeops-db
```

Esse valor será usado como variável de ambiente `CLOUD_SQL_CONNECTION_NAME`.

---

## 2. Secret Manager — Criação dos Secrets

Acesse **Secret Manager → Criar secret** e crie os dois secrets abaixo:

| Nome do secret   | Valor                                     |
|------------------|-------------------------------------------|
| `DB_PASSWORD`    | Senha do usuário de banco criado no passo 1.3 |
| `JWT_SECRET`     | String longa e aleatória para assinar os tokens JWT |

> Use senhas e segredos fortes em produção. Evite valores simples como `0000` ou `my-secret-key`.

---

## 3. IAM — Permissões da Conta de Serviço

A conta de serviço usada pelo Cloud Run (e pelo Cloud Build) precisa das seguintes permissões:

- **Cloud SQL Client** — para conectar ao banco via socket
- **Secret Manager Secret Accessor** — para ler os secrets
- **Editor** (ou roles específicas) — para deploy pelo Cloud Build

Acesse **IAM e Administrador → IAM**, localize a conta de serviço padrão do Compute Engine (formato `[número]@developer.gserviceaccount.com`) e adicione os papéis acima.

---

## 4. Alterações no Projeto

### 4.1 `pom.xml` — Dependências adicionadas

```xml
<!-- Conector Cloud SQL para PostgreSQL via Unix socket -->
<dependency>
    <groupId>com.google.cloud.sql</groupId>
    <artifactId>postgres-socket-factory</artifactId>
    <version>1.19.1</version>
</dependency>

<!-- Swagger / OpenAPI UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

### 4.2 `src/main/resources/application.properties`

Configurações base (compartilhadas entre todos os perfis):

```properties
spring.application.name=StoreOpsBackEnd
server.port=${PORT:8080}

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

api.security.token.secret=${JWT_SECRET:my-secret-key}
```

> `${PORT:8080}` é obrigatório — o Cloud Run injeta a porta via variável de ambiente `PORT`.

### 4.3 `src/main/resources/application-local.properties`

Configurações para desenvolvimento local:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/StoreOpsDB
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:0000}
spring.jpa.hibernate.ddl-auto=update
```

Para rodar localmente com esse perfil:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 4.4 `src/main/resources/application-prod.properties`

Configurações para produção (Cloud Run + Cloud SQL):

```properties
spring.datasource.url=jdbc:postgresql:///StoreOpsDB?cloudSqlInstance=${CLOUD_SQL_CONNECTION_NAME}&socketFactory=com.google.cloud.sql.postgres.SocketFactory
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=none
spring.flyway.repair-on-migrate=true
```

> A conexão com o Cloud SQL usa Unix socket via `postgres-socket-factory` — não é necessário IP público nem senha de instância.  
> `ddl-auto=none` porque o Flyway é responsável por toda a DDL em produção.  
> `repair-on-migrate=true` faz o Flyway limpar automaticamente entradas de migrações com falha antes de tentar aplicá-las novamente.

### 4.5 `SecurityConfigurations.java` — Liberar Swagger

Adicionar nas permissões públicas do Spring Security:

```java
.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
```

### 4.6 `OpenApiConfig.java` — Configuração do Swagger com JWT

Criar a classe `src/main/java/com/store_ops_backend/infra/config/OpenApiConfig.java`:

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
            .info(new Info()
                .title("StoreOps API")
                .description("API para gerenciamento de estabelecimentos de alimentação")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

O Swagger UI fica disponível em `/swagger-ui/index.html`.

---

## 5. Dockerfile

Criado na raiz do projeto (`/Dockerfile`):

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build em dois estágios: o primeiro compila o JAR com Maven, o segundo roda apenas o JRE para uma imagem menor.

---

## 6. Cloud Run — Configuração do Serviço

Acesse **Cloud Run → Criar serviço**.

### 6.1 Origem do container

Selecione **Implantar continuamente a partir de um repositório** para integrar com o Cloud Build.

### 6.2 Conexão com Cloud SQL

Na aba **Conexões**, adicione a instância Cloud SQL criada no passo 1.

### 6.3 Variáveis de ambiente

| Variável                  | Tipo          | Valor / Secret              |
|---------------------------|---------------|-----------------------------|
| `SPRING_PROFILES_ACTIVE`  | Variável      | `prod`                      |
| `CLOUD_SQL_CONNECTION_NAME` | Variável    | Ex: `projeto:região:instância` |
| `DB_USER`                 | Variável      | Ex: `StoreOpsUser`          |
| `DB_PASSWORD`             | Secret        | Secret `DB_PASSWORD`        |
| `JWT_SECRET`              | Secret        | Secret `JWT_SECRET`         |

> Variáveis simples ficam como "variável de ambiente". Credenciais ficam como referência ao Secret Manager.

---

## 7. Cloud Build — CI/CD

### 7.1 Conectar repositório

No console do Cloud Build, acesse **Gatilhos → Conectar repositório** e autorize o GitHub.

### 7.2 Criar gatilho

- **Tipo**: Dockerfile
- **Branch**: `^master$`
- **Localização do Dockerfile**: `/Dockerfile`

Com isso, cada push na branch `master` dispara automaticamente um novo build e deploy no Cloud Run.

---

## 8. CORS — Configuração de Origens Permitidas

Para adicionar a URL do frontend em produção sem alterar o código, use a variável de ambiente `CORS_ALLOWED_ORIGINS` no Cloud Run:

| Variável               | Valor                                    |
|------------------------|------------------------------------------|
| `CORS_ALLOWED_ORIGINS` | `https://seu-frontend.com` |

Múltiplas origens separadas por vírgula são suportadas.

---

## 9. Observações e Limitações Conhecidas

### Sistema de arquivos efêmero

O Cloud Run não persiste arquivos entre instâncias. O diretório `uploads/` (usado para imagens de produtos) é perdido a cada novo deploy ou escalonamento. Para produção real, migrar para o **Google Cloud Storage**.

### JWT Secret

Use uma string longa e aleatória para o `JWT_SECRET` em produção. Exemplo de geração:

```bash
openssl rand -base64 64
```

### Swagger em produção

O Swagger UI fica acessível em:
```
https://<url-do-cloud-run>/swagger-ui/index.html
```

Se quiser desabilitar em produção, adicione ao `application-prod.properties`:
```properties
springdoc.swagger-ui.enabled=false
```
