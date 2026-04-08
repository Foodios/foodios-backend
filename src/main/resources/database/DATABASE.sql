
CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE roles
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE authorities
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

INSERT INTO authorities (created_at, updated_at, code, description, enabled, name)
VALUES
-- System / platform
(NOW(), NOW(), 'SYSTEM_MANAGE', 'Manage system-wide settings', TRUE, 'System Manage'),
(NOW(), NOW(), 'USER_VIEW', 'View users', TRUE, 'User View'),
(NOW(), NOW(), 'USER_CREATE', 'Create users', TRUE, 'User Create'),
(NOW(), NOW(), 'USER_UPDATE', 'Update users', TRUE, 'User Update'),
(NOW(), NOW(), 'USER_DELETE', 'Delete users', TRUE, 'User Delete'),
(NOW(), NOW(), 'ROLE_VIEW', 'View roles', TRUE, 'Role View'),
(NOW(), NOW(), 'ROLE_CREATE', 'Create roles', TRUE, 'Role Create'),
(NOW(), NOW(), 'ROLE_UPDATE', 'Update roles', TRUE, 'Role Update'),
(NOW(), NOW(), 'ROLE_DELETE', 'Delete roles', TRUE, 'Role Delete'),
(NOW(), NOW(), 'AUTHORITY_VIEW', 'View authorities', TRUE, 'Authority View'),
(NOW(), NOW(), 'AUTHORITY_ASSIGN', 'Assign authorities to roles', TRUE, 'Authority Assign'),

-- Merchant
(NOW(), NOW(), 'MERCHANT_VIEW', 'View merchant information', TRUE, 'Merchant View'),
(NOW(), NOW(), 'MERCHANT_CREATE', 'Create merchant', TRUE, 'Merchant Create'),
(NOW(), NOW(), 'MERCHANT_UPDATE', 'Update merchant', TRUE, 'Merchant Update'),
(NOW(), NOW(), 'MERCHANT_DELETE', 'Delete merchant', TRUE, 'Merchant Delete'),
(NOW(), NOW(), 'MERCHANT_APPROVE', 'Approve merchant onboarding', TRUE, 'Merchant Approve'),

-- Branch / store
(NOW(), NOW(), 'BRANCH_VIEW', 'View branch/store', TRUE, 'Branch View'),
(NOW(), NOW(), 'BRANCH_CREATE', 'Create branch/store', TRUE, 'Branch Create'),
(NOW(), NOW(), 'BRANCH_UPDATE', 'Update branch/store', TRUE, 'Branch Update'),
(NOW(), NOW(), 'BRANCH_DELETE', 'Delete branch/store', TRUE, 'Branch Delete'),

-- Menu / category / food
(NOW(), NOW(), 'CATEGORY_VIEW', 'View menu categories', TRUE, 'Category View'),
(NOW(), NOW(), 'CATEGORY_CREATE', 'Create menu categories', TRUE, 'Category Create'),
(NOW(), NOW(), 'CATEGORY_UPDATE', 'Update menu categories', TRUE, 'Category Update'),
(NOW(), NOW(), 'CATEGORY_DELETE', 'Delete menu categories', TRUE, 'Category Delete'),

(NOW(), NOW(), 'FOOD_VIEW', 'View food items', TRUE, 'Food View'),
(NOW(), NOW(), 'FOOD_CREATE', 'Create food items', TRUE, 'Food Create'),
(NOW(), NOW(), 'FOOD_UPDATE', 'Update food items', TRUE, 'Food Update'),
(NOW(), NOW(), 'FOOD_DELETE', 'Delete food items', TRUE, 'Food Delete'),
(NOW(), NOW(), 'FOOD_PUBLISH', 'Publish or unpublish food items', TRUE, 'Food Publish'),

-- Inventory
(NOW(), NOW(), 'INVENTORY_VIEW', 'View inventory', TRUE, 'Inventory View'),
(NOW(), NOW(), 'INVENTORY_UPDATE', 'Update inventory', TRUE, 'Inventory Update'),

-- Order
(NOW(), NOW(), 'ORDER_VIEW', 'View orders', TRUE, 'Order View'),
(NOW(), NOW(), 'ORDER_CREATE', 'Create orders', TRUE, 'Order Create'),
(NOW(), NOW(), 'ORDER_UPDATE', 'Update orders', TRUE, 'Order Update'),
(NOW(), NOW(), 'ORDER_CANCEL', 'Cancel orders', TRUE, 'Order Cancel'),
(NOW(), NOW(), 'ORDER_ASSIGN_DRIVER', 'Assign driver to order', TRUE, 'Order Assign Driver'),
(NOW(), NOW(), 'ORDER_ACCEPT', 'Accept incoming order', TRUE, 'Order Accept'),
(NOW(), NOW(), 'ORDER_PREPARE', 'Prepare order in kitchen', TRUE, 'Order Prepare'),
(NOW(), NOW(), 'ORDER_COMPLETE', 'Mark order as completed', TRUE, 'Order Complete'),

-- Delivery
(NOW(), NOW(), 'DELIVERY_VIEW', 'View delivery jobs', TRUE, 'Delivery View'),
(NOW(), NOW(), 'DELIVERY_ACCEPT', 'Accept delivery assignment', TRUE, 'Delivery Accept'),
(NOW(), NOW(), 'DELIVERY_PICKUP', 'Mark order as picked up', TRUE, 'Delivery Pickup'),
(NOW(), NOW(), 'DELIVERY_COMPLETE', 'Mark order as delivered', TRUE, 'Delivery Complete'),

-- Payments / finance
(NOW(), NOW(), 'PAYMENT_VIEW', 'View payment information', TRUE, 'Payment View'),
(NOW(), NOW(), 'PAYMENT_REFUND', 'Process refunds', TRUE, 'Payment Refund'),
(NOW(), NOW(), 'FINANCE_VIEW', 'View finance reports', TRUE, 'Finance View'),
(NOW(), NOW(), 'PAYOUT_VIEW', 'View merchant payouts', TRUE, 'Payout View'),
(NOW(), NOW(), 'PAYOUT_PROCESS', 'Process merchant payouts', TRUE, 'Payout Process'),

-- Promotion
(NOW(), NOW(), 'PROMOTION_VIEW', 'View promotions', TRUE, 'Promotion View'),
(NOW(), NOW(), 'PROMOTION_CREATE', 'Create promotions', TRUE, 'Promotion Create'),
(NOW(), NOW(), 'PROMOTION_UPDATE', 'Update promotions', TRUE, 'Promotion Update'),
(NOW(), NOW(), 'PROMOTION_DELETE', 'Delete promotions', TRUE, 'Promotion Delete'),

-- Review / support
(NOW(), NOW(), 'REVIEW_VIEW', 'View reviews and ratings', TRUE, 'Review View'),
(NOW(), NOW(), 'REVIEW_MODERATE', 'Moderate reviews and ratings', TRUE, 'Review Moderate'),
(NOW(), NOW(), 'SUPPORT_TICKET_VIEW', 'View support tickets', TRUE, 'Support Ticket View'),
(NOW(), NOW(), 'SUPPORT_TICKET_UPDATE', 'Update support tickets', TRUE, 'Support Ticket Update'),

-- Reports / dashboard
(NOW(), NOW(), 'REPORT_VIEW_PLATFORM', 'View platform-level reports', TRUE, 'Platform Report View'),
(NOW(), NOW(), 'REPORT_VIEW_MERCHANT', 'View merchant-level reports', TRUE, 'Merchant Report View'),
(NOW(), NOW(), 'REPORT_VIEW_BRANCH', 'View branch-level reports', TRUE, 'Branch Report View'),

-- Customer
(NOW(), NOW(), 'PROFILE_VIEW', 'View own profile', TRUE, 'Profile View'),
(NOW(), NOW(), 'PROFILE_UPDATE', 'Update own profile', TRUE, 'Profile Update'),
(NOW(), NOW(), 'CART_MANAGE', 'Manage shopping cart', TRUE, 'Cart Manage'),
(NOW(), NOW(), 'CHECKOUT', 'Place order through checkout', TRUE, 'Checkout'),
(NOW(), NOW(), 'ORDER_TRACK', 'Track own orders', TRUE, 'Order Track');

INSERT INTO roles (created_at, updated_at, description, name, code, enabled)
VALUES
    (NOW(), NOW(), 'Full system access across the entire multi-merchant platform', 'Super Admin', 'SUPER_ADMIN', TRUE),
    (NOW(), NOW(), 'Platform operations administrator', 'Platform Admin', 'PLATFORM_ADMIN', TRUE),
    (NOW(), NOW(), 'Platform support and customer issue handling', 'Support Agent', 'SUPPORT_AGENT', TRUE),

    (NOW(), NOW(), 'Merchant owner with full access to their merchant data', 'Merchant Owner', 'MERCHANT_OWNER', TRUE),
    (NOW(), NOW(), 'Merchant manager with operational control over stores, menus, and orders', 'Merchant Manager', 'MERCHANT_MANAGER', TRUE),
    (NOW(), NOW(), 'Branch manager responsible for one branch/store', 'Branch Manager', 'BRANCH_MANAGER', TRUE),
    (NOW(), NOW(), 'Store staff handling in-store operational tasks', 'Store Staff', 'STORE_STAFF', TRUE),

    (NOW(), NOW(), 'Delivery driver handling assigned delivery orders', 'Driver', 'DRIVER', TRUE),
    (NOW(), NOW(), 'End customer using the food delivery platform', 'Customer', 'CUSTOMER', TRUE);


INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         CROSS JOIN authorities a
WHERE r.code = 'SUPER_ADMIN';


INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'USER_VIEW','USER_CREATE','USER_UPDATE',
                                          'ROLE_VIEW','ROLE_CREATE','ROLE_UPDATE',
                                          'AUTHORITY_VIEW','AUTHORITY_ASSIGN',
                                          'MERCHANT_VIEW','MERCHANT_CREATE','MERCHANT_UPDATE','MERCHANT_APPROVE',
                                          'BRANCH_VIEW','BRANCH_CREATE','BRANCH_UPDATE',
                                          'FOOD_VIEW',
                                          'ORDER_VIEW','ORDER_UPDATE','ORDER_CANCEL','ORDER_ASSIGN_DRIVER',
                                          'DELIVERY_VIEW',
                                          'PAYMENT_VIEW','PAYMENT_REFUND',
                                          'FINANCE_VIEW','PAYOUT_VIEW','PAYOUT_PROCESS',
                                          'PROMOTION_VIEW','PROMOTION_CREATE','PROMOTION_UPDATE','PROMOTION_DELETE',
                                          'REVIEW_VIEW','REVIEW_MODERATE',
                                          'SUPPORT_TICKET_VIEW','SUPPORT_TICKET_UPDATE',
                                          'REPORT_VIEW_PLATFORM'
    )
WHERE r.code = 'PLATFORM_ADMIN';

INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'USER_VIEW',
                                          'ORDER_VIEW','ORDER_UPDATE','ORDER_CANCEL',
                                          'PAYMENT_VIEW','PAYMENT_REFUND',
                                          'REVIEW_VIEW','REVIEW_MODERATE',
                                          'SUPPORT_TICKET_VIEW','SUPPORT_TICKET_UPDATE',
                                          'PROFILE_VIEW'
    )
WHERE r.code = 'SUPPORT_AGENT';


INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'MERCHANT_VIEW','MERCHANT_UPDATE',
                                          'BRANCH_VIEW','BRANCH_CREATE','BRANCH_UPDATE','BRANCH_DELETE',
                                          'CATEGORY_VIEW','CATEGORY_CREATE','CATEGORY_UPDATE','CATEGORY_DELETE',
                                          'FOOD_VIEW','FOOD_CREATE','FOOD_UPDATE','FOOD_DELETE','FOOD_PUBLISH',
                                          'INVENTORY_VIEW','INVENTORY_UPDATE',
                                          'ORDER_VIEW','ORDER_UPDATE','ORDER_CANCEL','ORDER_ACCEPT','ORDER_PREPARE','ORDER_COMPLETE',
                                          'DELIVERY_VIEW',
                                          'PAYMENT_VIEW',
                                          'FINANCE_VIEW','PAYOUT_VIEW',
                                          'PROMOTION_VIEW','PROMOTION_CREATE','PROMOTION_UPDATE','PROMOTION_DELETE',
                                          'REVIEW_VIEW',
                                          'REPORT_VIEW_MERCHANT'
    )
WHERE r.code = 'MERCHANT_OWNER';

INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'MERCHANT_VIEW',
                                          'BRANCH_VIEW','BRANCH_CREATE','BRANCH_UPDATE',
                                          'CATEGORY_VIEW','CATEGORY_CREATE','CATEGORY_UPDATE',
                                          'FOOD_VIEW','FOOD_CREATE','FOOD_UPDATE','FOOD_PUBLISH',
                                          'INVENTORY_VIEW','INVENTORY_UPDATE',
                                          'ORDER_VIEW','ORDER_UPDATE','ORDER_ACCEPT','ORDER_PREPARE','ORDER_COMPLETE',
                                          'DELIVERY_VIEW',
                                          'PAYMENT_VIEW',
                                          'PROMOTION_VIEW','PROMOTION_CREATE','PROMOTION_UPDATE',
                                          'REVIEW_VIEW',
                                          'REPORT_VIEW_MERCHANT'
    )
WHERE r.code = 'MERCHANT_MANAGER';

INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'BRANCH_VIEW','BRANCH_UPDATE',
                                          'CATEGORY_VIEW',
                                          'FOOD_VIEW','FOOD_CREATE','FOOD_UPDATE',
                                          'INVENTORY_VIEW','INVENTORY_UPDATE',
                                          'ORDER_VIEW','ORDER_UPDATE','ORDER_ACCEPT','ORDER_PREPARE','ORDER_COMPLETE',
                                          'DELIVERY_VIEW',
                                          'REVIEW_VIEW',
                                          'REPORT_VIEW_BRANCH'
    )
WHERE r.code = 'BRANCH_MANAGER';


INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'FOOD_VIEW',
                                          'INVENTORY_VIEW',
                                          'ORDER_VIEW','ORDER_UPDATE','ORDER_ACCEPT',
                                          'PROFILE_VIEW'
    )
WHERE r.code = 'STORE_STAFF';

INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'ORDER_VIEW','ORDER_PREPARE','ORDER_COMPLETE',
                                          'FOOD_VIEW',
                                          'PROFILE_VIEW'
    )
WHERE r.code = 'KITCHEN_STAFF';

INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'DELIVERY_VIEW','DELIVERY_ACCEPT','DELIVERY_PICKUP','DELIVERY_COMPLETE',
                                          'ORDER_VIEW',
                                          'PROFILE_VIEW','PROFILE_UPDATE'
    )
WHERE r.code = 'DRIVER';

INSERT INTO role_authorities (role_id, authority_id)
SELECT r.id, a.id
FROM roles r
         JOIN authorities a ON a.code IN (
                                          'PROFILE_VIEW','PROFILE_UPDATE',
                                          'FOOD_VIEW',
                                          'CART_MANAGE',
                                          'CHECKOUT',
                                          'ORDER_CREATE','ORDER_VIEW','ORDER_TRACK',
                                          'REVIEW_VIEW'
    )
WHERE r.code = 'CUSTOMER';
