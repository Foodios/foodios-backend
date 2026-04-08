DO $$
BEGIN
    -- Fix for check constraint if it exists
    ALTER TABLE merchant_members DROP CONSTRAINT IF EXISTS merchant_members_role_check;
    ALTER TABLE merchant_members ADD CONSTRAINT merchant_members_role_check CHECK (role IN ('OWNER', 'MANAGER', 'STAFF', 'DRIVER'));
EXCEPTION
    WHEN undefined_table THEN
        RAISE NOTICE 'Table merchant_members not found, skipping constraint update.';
END $$;

DO $$
DECLARE
    v_merchant_id UUID := '8b04d3c9-4d21-405d-9e86-e48ae9652b67'; -- UPDATE THIS ID
    v_role_id UUID;
    v_user1_id UUID := gen_random_uuid();
    v_user2_id UUID := gen_random_uuid();
    v_user3_id UUID := gen_random_uuid();
    v_user4_id UUID := gen_random_uuid();
    v_user5_id UUID := gen_random_uuid();
    v_user6_id UUID := gen_random_uuid();
    v_password_hash VARCHAR := '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DM99a7yXzXSm'; -- 'password'
BEGIN
    -- Get Driver Role ID
    SELECT id INTO v_role_id FROM roles WHERE code = 'DRIVER';

    IF v_role_id IS NULL THEN
        RAISE EXCEPTION 'Role DRIVER not found. Please run DATABASE.sql first.';
    END IF;

    -- 1. Insert Users
    INSERT INTO app_users (id, username, email, phone, password_hash, full_name, profile_completed, status, created_at, updated_at)
    VALUES
    (v_user1_id, 'driver.toan', 'toan.driver@example.com', '0901111001', v_password_hash, 'Nguyễn Văn Toàn', TRUE, 'ACTIVE', NOW(), NOW()),
    (v_user2_id, 'driver.hung', 'hung.driver@example.com', '0901111002', v_password_hash, 'Trần Quốc Hùng', TRUE, 'ACTIVE', NOW(), NOW()),
    (v_user3_id, 'driver.linh', 'linh.driver@example.com', '0901111003', v_password_hash, 'Lê Thị Mỹ Linh', TRUE, 'ACTIVE', NOW(), NOW()),
    (v_user4_id, 'driver.dung', 'dung.driver@example.com', '0901111004', v_password_hash, 'Phạm Anh Dũng', TRUE, 'ACTIVE', NOW(), NOW()),
    (v_user5_id, 'driver.thanh', 'thanh.driver@example.com', '0901111005', v_password_hash, 'Đặng Hoàng Thanh', TRUE, 'ACTIVE', NOW(), NOW()),
    (v_user6_id, 'driver.minh', 'minh.driver@example.com', '0901111006', v_password_hash, 'Vũ Quang Minh', TRUE, 'ACTIVE', NOW(), NOW());

    -- 2. Assign System Roles
    INSERT INTO user_roles (user_id, role_id, assigned_at)
    VALUES
    (v_user1_id, v_role_id, NOW()),
    (v_user2_id, v_role_id, NOW()),
    (v_user3_id, v_role_id, NOW()),
    (v_user4_id, v_role_id, NOW()),
    (v_user5_id, v_role_id, NOW()),
    (v_user6_id, v_role_id, NOW());

    -- 3. Associate with Merchant (if merchant ID is provided and valid)
    IF v_merchant_id IS NOT NULL AND EXISTS (SELECT 1 FROM merchants WHERE id = v_merchant_id) THEN
        INSERT INTO merchant_members (id, merchant_id, user_id, role, status, assigned_at, created_at, updated_at)
        VALUES
        (gen_random_uuid(), v_merchant_id, v_user1_id, 'DRIVER', 'ACTIVE', NOW(), NOW(), NOW()),
        (gen_random_uuid(), v_merchant_id, v_user2_id, 'DRIVER', 'ACTIVE', NOW(), NOW(), NOW()),
        (gen_random_uuid(), v_merchant_id, v_user3_id, 'DRIVER', 'ACTIVE', NOW(), NOW(), NOW()),
        (gen_random_uuid(), v_merchant_id, v_user4_id, 'DRIVER', 'ACTIVE', NOW(), NOW(), NOW()),
        (gen_random_uuid(), v_merchant_id, v_user5_id, 'DRIVER', 'ACTIVE', NOW(), NOW(), NOW()),
        (gen_random_uuid(), v_merchant_id, v_user6_id, 'DRIVER', 'ACTIVE', NOW(), NOW(), NOW());

        RAISE NOTICE 'Added 6 drivers to merchant %', v_merchant_id;
    ELSE
        RAISE NOTICE 'Added 6 drivers to system, but NO merchant association made. Please update v_merchant_id in script.';
    END IF;

END $$;
