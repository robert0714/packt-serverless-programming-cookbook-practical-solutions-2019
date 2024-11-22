# 定義需要導入的 User Pool ID 變數
variable "user_pool_id" {
  description = "The ID of the User Pool to attach the client to"
  type        = string
}

# 建立 Cognito User Pool Client
resource "aws_cognito_user_pool_client" "my_client" {
  name = "My Cognito User Pool Client"

  user_pool_id = var.user_pool_id

  explicit_auth_flows = [
    "USER_PASSWORD_AUTH"
  ]

  refresh_token_validity = 30
}

# 輸出 Client ID
output "client_id" {
  description = "Cognito user pool Client"
  value       = aws_cognito_user_pool_client.my_client.id
}