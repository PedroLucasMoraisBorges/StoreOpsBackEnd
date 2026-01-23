ALTER TABLE people
    ADD COLUMN employee_person_id TEXT,
    ADD CONSTRAINT fk_people_employee_person
        FOREIGN KEY (employee_person_id)
        REFERENCES people (id);
