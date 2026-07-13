-- Companies
insert into company(id, name)
values (1001, 'Lanit'),
       (1002, 'IBM'),
       (1003, 'Oracle');

--- Orders
insert into orders(id, amount, customer_id)
values (2001, 10.00, 1001),
       (2002, 15.10, 1001),
       (2003, 100.00, 1002),
       (2004, 203.50, 1002),
       (2005, 10.00, 1003),
       (2006, 100.00, 1003);

-- Users
insert into users(id, login, password)
values (3001, 'user1', '$2a$10$L8BTPKYxARZdsv8DRTR7KeH.Q2CwqkpFd0FVKmAY4xwKzrBJ3ILUu'),
       (3002, 'user2', '$2a$10$L8BTPKYxARZdsv8DRTR7KeH.Q2CwqkpFd0FVKmAY4xwKzrBJ3ILUu'),
       (3003, 'admin', '$2a$10$L8BTPKYxARZdsv8DRTR7KeH.Q2CwqkpFd0FVKmAY4xwKzrBJ3ILUu');

-- User roles
insert into user_roles(user_id, roles)
values (3001, 'user'),
       (3002, 'user'),
       (3002, 'manager'),
       (3003, 'admin');

-- User-Company
insert into user_company(user_id, companies_id)
values (3001, 1001),
       (3001, 1002),
       (3002, 1003),
       (3003, 1001),
       (3003, 1002),
       (3003, 1003);
