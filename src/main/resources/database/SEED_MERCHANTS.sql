DO $$
DECLARE
    v_m1_id UUID := gen_random_uuid();
    v_m2_id UUID := gen_random_uuid();
    v_m3_id UUID := gen_random_uuid();
    v_m4_id UUID := gen_random_uuid();
    v_m5_id UUID := gen_random_uuid();
    v_m6_id UUID := gen_random_uuid();
    v_m7_id UUID := gen_random_uuid();
BEGIN
    -- 1. Insert Merchants
    INSERT INTO merchants (id, display_name, legal_name, description, slug, logo_url, cuisine_category, status, created_at, updated_at)
    VALUES
    (v_m1_id, 'Phở Sướng - Đinh Liệt', 'Công ty TNHH Phở Sướng Hà Nội', 'Phở bò truyền thống ngon bậc nhất phố cổ.', 'pho-suong-dinh-liet', 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=500', 'Vietnamese, Noodles', 'ACTIVE', NOW(), NOW()),
    (v_m2_id, 'Bún Chả Cửa Đông', 'Hộ Kinh Doanh Bún Chả Cửa Đông', 'Bún chả gia truyền với chả que tre đặc sắc.', 'bun-cha-cua-dong', 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500', 'Vietnamese, Grilled Pork', 'ACTIVE', NOW(), NOW()),
    (v_m3_id, 'Pizza 4P''s - Hai Bà Trưng', 'Công ty CP Pizza 4P''s', 'Pizza thủ công mang đậm phong cách Nhật - Ý.', 'pizza-4ps-hai-ba-trung', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500', 'Italian, Pizza', 'ACTIVE', NOW(), NOW()),
    (v_m4_id, 'Highlands Coffee - Hàm Cá Mập', 'Công ty CP Coffee Concepts Việt Nam', 'Thương hiệu cà phê hàng đầu Việt Nam.', 'highlands-coffee-ham-ca-map', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=500', 'Cafe, Drinks', 'ACTIVE', NOW(), NOW()),
    (v_m5_id, 'The Coffee House - Cao Thắng', 'Công ty CP Seedcom', 'Nơi kết nối cộng đồng yêu cà phê.', 'the-coffee-house-cao-thang', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500', 'Cafe, Drinks', 'ACTIVE', NOW(), NOW()),
    (v_m6_id, 'Cơm Tấm Bụi Sài Gòn', 'Công ty TNHH Ẩm Thực Bụi', 'Cơm tấm chuẩn vị Sài Gòn với sườn nướng mỡ hành.', 'com-tam-bui-sai-gon', 'https://images.unsplash.com/photo-1541529086526-db283c563270?w=500', 'Vietnamese, Rice', 'ACTIVE', NOW(), NOW()),
    (v_m7_id, 'Bánh Mì Phượng Hội An', 'Hộ Kinh Doanh Bánh Mì Phượng', 'Bánh mì nổi tiếng nhất Hội An, được Anthony Bourdain khen ngợi.', 'banh-mi-phuong-hoi-an', 'https://images.unsplash.com/photo-1600454021970-351eff4a6554?w=500', 'Vietnamese, Sandwich', 'ACTIVE', NOW(), NOW());

    -- 2. Insert Merchant Addresses
    -- Table: merchant_addresses (Inherits from BaseEntity, Address fields in snapshot)
    -- id, created_at, updated_at, label, is_default, merchant_id, 
    -- snapshot: contact_name, contact_phone, line1, line2, ward, district, city, province, postal_code, country, latitude, longitude

    INSERT INTO merchant_addresses (id, merchant_id, label, is_default, contact_name, contact_phone, line1, line2, ward, district, city, province, postal_code, country, latitude, longitude, created_at, updated_at)
    VALUES
    (gen_random_uuid(), v_m1_id, 'Trụ sở chính', TRUE, 'Ông Sướng', '02438288151', '24 Đinh Liệt', NULL, 'Hàng Đào', 'Hoàn Kiếm', 'Hà Nội', 'Hà Nội', '100000', 'VN', 21.0321, 105.8524, NOW(), NOW()),
    (gen_random_uuid(), v_m2_id, 'Cửa hàng', TRUE, 'Chủ quán', '0903255152', '41 Cửa Đông', NULL, 'Cửa Đông', 'Hoàn Kiếm', 'Hà Nội', 'Hà Nội', '100000', 'VN', 21.0345, 105.8456, NOW(), NOW()),
    (gen_random_uuid(), v_m3_id, 'Chi nhánh', TRUE, 'Quản lý', '02836220500', '43 Hai Bà Trưng', NULL, 'Bến Nghé', 'Quận 1', 'Hồ Chí Minh', 'TP. HCM', '700000', 'VN', 10.7798, 106.7023, NOW(), NOW()),
    (gen_random_uuid(), v_m4_id, 'Chi nhánh', TRUE, 'Quản lý', '19001755', '8 Đinh Tiên Hoàng', NULL, 'Hàng Bạc', 'Hoàn Kiếm', 'Hà Nội', 'Hà Nội', '100000', 'VN', 21.0315, 105.8521, NOW(), NOW()),
    (gen_random_uuid(), v_m5_id, 'Chi nhánh Cao Thắng', TRUE, 'Quản lý', '18006936', '12 Cao Thắng', NULL, 'Phường 5', 'Quận 3', 'Hồ Chí Minh', 'TP. HCM', '700000', 'VN', 10.7725, 106.6812, NOW(), NOW()),
    (gen_random_uuid(), v_m6_id, 'Cửa hàng chính', TRUE, 'Chủ quán', '0909123456', '100 Thạch Thị Thanh', NULL, 'Tân Định', 'Quận 1', 'Hồ Chí Minh', 'TP. HCM', '700000', 'VN', 10.7901, 106.6908, NOW(), NOW()),
    (gen_random_uuid(), v_m7_id, 'Cửa hàng chính', TRUE, 'Chị Phượng', '0905316356', '2B Phan Chu Trinh', NULL, 'Cẩm Châu', 'Hội An', 'Quảng Nam', 'Quảng Nam', '560000', 'VN', 15.8794, 108.3345, NOW(), NOW());

    RAISE NOTICE 'Seed successful: 7 merchants and their addresses added.';
END $$;
