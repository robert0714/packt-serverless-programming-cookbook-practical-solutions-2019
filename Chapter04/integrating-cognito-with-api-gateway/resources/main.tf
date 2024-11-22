# Provider configuration
provider "aws" { 
}

# 取得 AWS 帳號 ID
data "aws_caller_identity" "current" {}

# 取得當前 region
data "aws_region" "current" {}

# API Gateway REST API
resource "aws_api_gateway_rest_api" "greeting_api" {
  name        = "Greeting API"
  description = "API for greeting an user"
}

# Cognito Authorizer
resource "aws_api_gateway_authorizer" "cognito" {
  name          = "FirstCognitoAuthorizer"
  rest_api_id   = aws_api_gateway_rest_api.greeting_api.id
  type          = "COGNITO_USER_POOLS"
  provider_arns = ["arn:aws:cognito-idp:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:userpool/${data.aws_cognito_user_pools.existing.id}"]

  identity_source = "method.request.header.Authorization"
}

# Data source for existing Cognito User Pool
data "aws_cognito_user_pools" "existing" {
  name = "your-user-pool-name" # 需要替換成實際的 User Pool 名稱
}

# API Gateway Resource
resource "aws_api_gateway_resource" "greeting" {
  rest_api_id = aws_api_gateway_rest_api.greeting_api.id
  parent_id   = aws_api_gateway_rest_api.greeting_api.root_resource_id
  path_part   = "greeting"
}

# API Gateway Method
resource "aws_api_gateway_method" "greeting_get" {
  rest_api_id   = aws_api_gateway_rest_api.greeting_api.id
  resource_id   = aws_api_gateway_resource.greeting.id
  http_method   = "GET"
  authorization = "COGNITO_USER_POOLS"
  authorizer_id = aws_api_gateway_authorizer.cognito.id
}

# API Gateway Integration
resource "aws_api_gateway_integration" "greeting_integration" {
  rest_api_id = aws_api_gateway_rest_api.greeting_api.id
  resource_id = aws_api_gateway_resource.greeting.id
  http_method = aws_api_gateway_method.greeting_get.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = "{\"statusCode\": 200}"
  }
}

# API Gateway Integration Response
resource "aws_api_gateway_integration_response" "greeting_integration_response" {
  rest_api_id = aws_api_gateway_rest_api.greeting_api.id
  resource_id = aws_api_gateway_resource.greeting.id
  http_method = aws_api_gateway_method.greeting_get.http_method
  status_code = "200"

  response_templates = {
    "application/json" = "{\"message\": \"Welcome $context.authorizer.claims.given_name\" }"
  }

  depends_on = [aws_api_gateway_integration.greeting_integration]
}

# API Gateway Method Response
resource "aws_api_gateway_method_response" "greeting_response" {
  rest_api_id = aws_api_gateway_rest_api.greeting_api.id
  resource_id = aws_api_gateway_resource.greeting.id
  http_method = aws_api_gateway_method.greeting_get.http_method
  status_code = "200"
}

# API Gateway Deployment
resource "aws_api_gateway_deployment" "greeting" {
  rest_api_id = aws_api_gateway_rest_api.greeting_api.id

  depends_on = [
    aws_api_gateway_integration.greeting_integration,
    aws_api_gateway_integration_response.greeting_integration_response,
    aws_api_gateway_method_response.greeting_response
  ]

  lifecycle {
    create_before_destroy = true
  }
}

# API Gateway Stage
resource "aws_api_gateway_stage" "dev" {
  deployment_id = aws_api_gateway_deployment.greeting.id
  rest_api_id   = aws_api_gateway_rest_api.greeting_api.id
  stage_name    = "dev"
  description   = "Dev Stage"
}

# Outputs
output "api_endpoint" {
  description = "API Gateway endpoint URL for dev stage"
  value       = "${aws_api_gateway_rest_api.greeting_api.execution_arn}/dev/greeting"
}