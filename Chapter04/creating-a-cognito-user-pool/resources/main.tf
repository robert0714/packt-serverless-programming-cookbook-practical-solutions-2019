# Configure AWS Provider
provider "aws" { 
}

# Create Cognito User Pool
resource "aws_cognito_user_pool" "my_user_pool" {
  name = "MyApp User Pool"

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_numbers   = true
    require_symbols   = true
    require_uppercase = true
  }

  auto_verified_attributes = ["email"]
  alias_attributes        = ["email"]

  verification_message_template {
    default_email_option = "CONFIRM_WITH_CODE"
    email_message        = "Your verification code from MyApp is {####}."
    email_subject        = "Your verification code from MyApp"
  }

  admin_create_user_config {
    allow_admin_create_user_only = false
    
    invite_message_template {
      email_message = "Your username for MyApp is {username} and password is {####}."
      email_subject = "Your temporary password for MyApp"
      sms_message   = "Your username for MyApp is {username} and password is {####}."
    }
  }

  # 設置密碼有效期
  user_pool_add_ons {
    advanced_security_mode = "OFF"
  }

  # 設置帳戶過期時間（以天為單位）
  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  tags = {
    Team = "Dev"
  }
}

# Output definitions
output "user_pool_id" {
  description = "Cognito user pool ID"
  value       = aws_cognito_user_pool.my_user_pool.id
}