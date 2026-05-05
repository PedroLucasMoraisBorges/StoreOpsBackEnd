# Auditoria Backend – StoreOps

> Revisado em 2026-04-20 (última atualização: 2026-04-20 — cardápio com pedidos online, mesas com pagamento, caixa unificado).

## Resumo

**Pontuação geral: 8/10**

O backend está bem estruturado: Spring Boot + Spring Security (JWT) + PostgreSQL + Flyway. GlobalExceptionHandler implementado, Bean Validation com @Valid nos controllers, módulos de Produtos, Estoque, Mesas/Sessões e Despesas todos implementados com migrations. Pendências menores: nomenclatura não-RESTful (herdada), sem verificação de autorização por empresa, sem índices em colunas de filtro frequente.

---

## Endpoints Mapeados

### AuthController — `/auth`
| Método | Rota | Função |
|--------|------|--------|
| POST | `/auth/login` | Autenticação JWT |
| POST | `/auth/register` | Registro de usuário |

### CompanyController — `/companies`
| Método | Rota | Função |
|--------|------|--------|
| POST | `/companies/create` | Criar empresa |
| GET | `/companies/getAll` | Listar empresas do usuário |
| GET | `/companies/get/{id}` | Buscar empresa |
| PUT | `/companies/update/{id}` | Atualizar empresa |

### CustomerController — `/customers`
| Método | Rota | Função |
|--------|------|--------|
| POST | `/customers/create/{companyId}` | Criar cliente |
| GET | `/customers/getAll/{companyId}` | Listar clientes |
| GET | `/customers/get/{companyId}/{customerId}` | Buscar cliente |
| PUT | `/customers/update/{companyId}/{customerId}` | Atualizar cliente |
| POST | `/customers/debit/{companyId}/{customerId}` | Registrar débito (fiado) |
| POST | `/customers/payment/{companyId}/{customerId}` | Registrar pagamento |
| GET | `/customers/transactions/{companyId}/{customerId}` | Listar transações |

### EmployeeController — `/employees`
| Método | Rota | Função |
|--------|------|--------|
| POST | `/employees/create/{companyId}` | Criar funcionário |
| GET | `/employees/getAll/{companyId}` | Listar funcionários |
| GET | `/employees/get/{companyId}/{userId}` | Buscar funcionário |
| PUT | `/employees/update/{companyId}/{userId}` | Atualizar funcionário |
| DELETE | `/employees/delete/{companyId}/{userId}` | Deletar funcionário |
| PUT | `/employees/alterStatus/{companyId}/{userId}` | Alterar status |
| GET | `/employees/transactions/{companyId}/{userId}` | Transações do funcionário |

### OrderController — `/orders`
| Método | Rota | Função |
|--------|------|--------|
| POST | `/orders/create/{companyId}` | Criar encomenda |
| GET | `/orders/getAll/{companyId}` | Listar encomendas |
| GET | `/orders/get/{companyId}/{orderId}` | Buscar encomenda |
| GET | `/orders/getByCustomer/{companyId}/{customerId}` | Encomendas por cliente |
| PUT | `/orders/update/{companyId}/{orderId}` | Atualizar encomenda |
| PUT | `/orders/status/{companyId}/{orderId}` | Atualizar status |
| POST | `/orders/items/{companyId}/{orderId}` | Adicionar itens |
| PUT | `/orders/items/remove/{companyId}/{orderId}/{itemId}` | Remover item |
| PUT | `/orders/payment/{companyId}/{orderId}?paymentMethodId=` | Registrar pagamento da encomenda |

### MenuController — `/menu` (público — sem autenticação)
| Método | Rota | Função |
|--------|------|--------|
| GET | `/menu/{slug}` | Buscar cardápio público pelo slug |
| POST | `/menu/{slug}/orders` | Criar pedido online (sem login) — cria People + Account + Order ONLINE |
| GET | `/menu/{slug}/orders/{orderId}` | Rastrear status do pedido pelo cliente |

### DashboardController — `/dashboard`
| Método | Rota | Função |
|--------|------|--------|
| GET | `/dashboard/summary?companyId=` | Resumo geral |

### ReportController — `/reports`
| Método | Rota | Função |
|--------|------|--------|
| GET | `/reports/orders?companyId=&dateFrom=&dateTo=` | PDF encomendas |
| GET | `/reports/fiado?companyId=&dateFrom=&dateTo=` | PDF fiado |
| GET | `/reports/customers?companyId=` | PDF clientes |
| GET | `/reports/employees?companyId=&dateFrom=&dateTo=` | PDF funcionários |

### PaymentMethodController — `/payment-methods`
| Método | Rota | Função |
|--------|------|--------|
| GET | `/payment-methods/getAll` | Listar métodos de pagamento |

### UserController — `/users`
| Método | Rota | Função |
|--------|------|--------|
| (não auditado em detalhe) | | |

---

## Cobertura Funcional das 8 Funcionalidades

| Funcionalidade | Cobertura de API |
|----------------|-----------------|
| Dashboard | ✅ KPIs diários + semanais + top clientes. Falta: comparativo %, alertas de estoque |
| Clientes / Fiado | ✅ CRUD completo + transações de débito/pagamento |
| Encomendas | ✅ CRUD + itens + status + pagamento vinculado (V18) + relatório PDF |
| Funcionários | ✅ CRUD + status + transações |
| Relatórios | ✅ PDF para encomendas, fiado, clientes, funcionários |
| Pagamentos / Caixa | ✅ `GET /transactions/getAll/{companyId}` + `ExpenseController` (V16) |
| Estoque | ✅ `ProductController` + `StockController` com alertas e histórico de movimentos |
| Despesas | ✅ `ExpenseController` — CRUD completo (V16, 2026-04-20) |
| Cardápio Digital | ✅ `MenuController` — GET cardápio público + POST pedido online + GET rastreamento |
| Comandas / Mesas | ✅ `TableController` — criar mesas, abrir/fechar sessão, itens em tempo real |
| PDV | ❌ Ausente |

---

## Problemas de Qualidade

### 1. Nomenclatura de Rotas Não-RESTful
- **Problema**: Usa `/getAll`, `/create`, `/get/{id}`, `/alterStatus` em vez dos verbos HTTP com paths simples
- **Impacto**: Viola convenção REST, dificulta integração com ferramentas e docs automáticos
- **Recomendação**:
  - `POST /customers/{companyId}` em vez de `POST /customers/create/{companyId}`
  - `GET /customers/{companyId}` em vez de `GET /customers/getAll/{companyId}`
  - `PATCH /employees/{companyId}/{userId}/status` em vez de `PUT /employees/alterStatus/{companyId}/{userId}`

### 2. Ausência de Validação de Input (Bean Validation)
- **Problema**: Nenhum DTO usa `@Valid`, `@NotBlank`, `@NotNull`, `@Size` ou similares
- **Impacto**: Dados inválidos chegam ao banco (nomes em branco, valores negativos, datas inválidas)
- **Exemplo**: `CreateOrderDTO` sem validação de `scheduledAt` obrigatório
- **Recomendação**: Adicionar `@Valid` nos `@RequestBody` e anotações nos DTOs

### 3. Sem Tratamento Global de Erros
- **Problema**: Apenas `ReportController` tem `@ExceptionHandler` localizado
- **Impacto**: Exceções não tratadas retornam stack trace ou respostas 500 sem mensagem útil
- **Recomendação**: Criar `@RestControllerAdvice` global para `EntityNotFoundException`, `IllegalArgumentException`, `DataIntegrityViolationException`

### 4. Sem @ResponseStatus nas Criações
- **Problema**: Endpoints de criação (`POST /*/create`) retornam `200 OK` em vez de `201 Created`
- **Impacto**: Clientes e ferramentas de monitoring não diferenciam criação de leitura
- **Recomendação**: Adicionar `@ResponseStatus(HttpStatus.CREATED)` ou usar `ResponseEntity.created()`

### 5. Possíveis N+1 Queries
- **Problema**: `Order` tem relacionamentos `@ManyToOne` sem `fetch = FetchType.LAZY` explícito + sem `@EntityGraph` nos repositórios
- **Impacto**: Listagem de encomendas com muitos registros pode gerar N queries extras para carregar `company`, `customer`, `attendant`
- **Recomendação**: Adicionar `@Query` com JOIN FETCH nos repositórios ou usar `@EntityGraph`

### 6. Autenticação
- **Status**: ✅ Implementada via JWT com `SecurityFilter` e `TokenService`
- **Observação**: Token armazenado no `localStorage` do frontend — considerar `httpOnly cookie` para proteção contra XSS em produção
- **Problema**: `CustomerController` não verifica se `companyId` pertence ao usuário autenticado (qualquer usuário autenticado pode acessar dados de outras empresas)

### 7. Banco de Dados
- **Positivo**: Flyway com migrações versionadas (V1–V13), chaves estrangeiras definidas
- **Problema**: Tabela `orders` usa `TEXT` como PK em vez de `UUID` nativo (inconsistência com `people` e `users` que usam `UUID`)
- **Problema**: Sem índices explícitos em colunas de filtro frequente: `orders.company_id`, `orders.status`, `people.company_id`, `account_transactions.account_id`
- **Recomendação**: Adicionar migração V14 com índices nas colunas mais consultadas

---

## Módulos Faltantes (Alta Prioridade restante)

### Dashboard — Alertas e Comparativo
Adicionar ao `DashboardService`: contagem de produtos com estoque abaixo do mínimo,
e comparativo percentual receita atual vs período anterior.

### Cardápio Digital
Criar tabela `menu_items` e endpoint público (sem autenticação) para exibição do cardápio.

### PDV
Interface de venda rápida integrada ao catálogo de produtos e às formas de pagamento.

---

## Prioridades de Backend (atualizadas 2026-04-20)

1. ✅ ~~Endpoint global de transações~~ — implementado
2. ✅ ~~GlobalExceptionHandler~~ — implementado
3. ✅ ~~Bean Validation~~ — implementado com @Valid nos controllers
4. ✅ ~~Catálogo de Produtos + Estoque~~ — implementado (V14)
5. ✅ ~~Comandas / Mesas~~ — implementado (V15)
6. ✅ ~~Despesas de caixa~~ — implementado (V16, 2026-04-20)
7. ✅ ~~Registro de pagamento em encomendas~~ — implementado (V18, 2026-04-20)
8. ✅ ~~Pedidos online pelo cardápio~~ — implementado (`POST /menu/{slug}/orders`, `GET /menu/{slug}/orders/{id}`)
9. ✅ ~~Mesas com forma de pagamento~~ — implementado (V19: `payment_method_id` + `paid_at` em `table_sessions`)
10. ✅ ~~Caixa unificado com encomendas e mesas~~ — `GET /tables/sessions/payments/{companyId}` + filtro `paidAt` em orders
11. **MÉDIA** — Verificação de autorização por empresa (usuário só acessa dados da própria empresa)
9. **MÉDIA** — Normalizar nomenclatura de rotas para REST convencional
10. **BAIXA** — PDV com interface de venda rápida
