-- SQL JOIN validation (reference only; not executed by the application).
-- Use these to verify relationships and indexes in the database.

-- 1) Get quotations for a procurement request
-- SELECT q.* FROM quotations q
-- INNER JOIN procurement_requests pr ON q.request_id = pr.id
-- WHERE pr.id = :requestId;

-- 2) Get order with vendor and customer (via quotation -> request -> customer, and quotation -> vendor)
-- SELECT o.order_number, o.total_amount, o.status,
--        v.company_name AS vendor_company, u_customer.email AS customer_email
-- FROM orders o
-- INNER JOIN quotations q ON o.quotation_id = q.id
-- INNER JOIN vendors v ON q.vendor_id = v.id
-- INNER JOIN procurement_requests pr ON q.request_id = pr.id
-- INNER JOIN users u_customer ON pr.customer_id = u_customer.id
-- WHERE o.id = :orderId;

-- 3) Get payments for an order
-- SELECT p.* FROM payments p
-- INNER JOIN orders o ON p.order_id = o.id
-- WHERE o.id = :orderId;
