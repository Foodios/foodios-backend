CREATE SEQUENCE merchant_application_form_code_seq;


CREATE OR REPLACE FUNCTION generate_merchant_application_form_code()
    RETURNS TEXT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_sequence BIGINT;
    v_sequence_formatted TEXT;
    v_form_date TEXT;
BEGIN
    v_sequence := nextval('merchant_application_form_code_seq');
    v_sequence_formatted := LPAD(v_sequence::TEXT, 6, '0');
    v_form_date := TO_CHAR(NOW(), 'YYYYMMDD');

    RETURN 'MAF-' || v_form_date || '-' || v_sequence_formatted;
END;
$$;