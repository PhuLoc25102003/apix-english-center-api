-- Convert tuition from course-total billing to monthly billing periods.

ALTER TABLE courses
    RENAME COLUMN default_tuition_fee TO default_monthly_tuition_fee;

CREATE TABLE tuition_packages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    number_of_months INTEGER NOT NULL,
    discount_type VARCHAR(30) NOT NULL DEFAULT 'NONE',
    discount_value DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMPTZ,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_tuition_package_months CHECK (number_of_months > 0),
    CONSTRAINT chk_tuition_package_discount_type
        CHECK (discount_type IN ('NONE', 'PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT chk_tuition_package_discount_value CHECK (discount_value >= 0)
);

INSERT INTO tuition_packages (
    name,
    number_of_months,
    discount_type,
    discount_value,
    description
) VALUES
    ('Monthly', 1, 'NONE', 0.00, 'Standard one-month tuition billing'),
    ('Three months advance', 3, 'PERCENTAGE', 10.00, 'Configurable advance-payment discount');

ALTER TABLE invoices
    ADD COLUMN class_id UUID REFERENCES classes(id) ON DELETE RESTRICT,
    ADD COLUMN enrollment_id UUID REFERENCES class_enrollments(id) ON DELETE SET NULL,
    ADD COLUMN tuition_package_id UUID REFERENCES tuition_packages(id) ON DELETE SET NULL,
    ADD COLUMN billing_start_month DATE,
    ADD COLUMN billing_end_month DATE,
    ADD COLUMN number_of_months INTEGER,
    ADD COLUMN monthly_fee DECIMAL(15, 2),
    ADD COLUMN subtotal_amount DECIMAL(15, 2),
    ADD COLUMN discount_type VARCHAR(30) NOT NULL DEFAULT 'NONE',
    ADD COLUMN discount_value DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00;

UPDATE invoices
SET billing_start_month = DATE_TRUNC('month', due_date)::DATE,
    billing_end_month = DATE_TRUNC('month', due_date)::DATE,
    number_of_months = 1,
    monthly_fee = total_amount,
    subtotal_amount = total_amount
WHERE billing_start_month IS NULL;

ALTER TABLE invoices
    ALTER COLUMN billing_start_month SET NOT NULL,
    ALTER COLUMN billing_end_month SET NOT NULL,
    ALTER COLUMN number_of_months SET NOT NULL,
    ALTER COLUMN monthly_fee SET NOT NULL,
    ALTER COLUMN subtotal_amount SET NOT NULL;

ALTER TABLE invoices DROP CONSTRAINT chk_invoices_status;
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_status
    CHECK (status IN ('UNPAID', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED', 'REFUNDED'));
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_monthly_billing
    CHECK (
        number_of_months > 0
        AND monthly_fee >= 0
        AND subtotal_amount >= 0
        AND discount_value >= 0
        AND discount_amount >= 0
        AND billing_start_month <= billing_end_month
    );
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_discount_type
    CHECK (discount_type IN ('NONE', 'PERCENTAGE', 'FIXED_AMOUNT'));

CREATE INDEX idx_tuition_packages_active ON tuition_packages(is_active, deleted_at);
CREATE INDEX idx_invoices_class_id ON invoices(class_id);
CREATE INDEX idx_invoices_enrollment_id ON invoices(enrollment_id);
CREATE INDEX idx_invoices_billing_period ON invoices(billing_start_month, billing_end_month);
CREATE INDEX idx_invoices_class_status ON invoices(class_id, status);
