# Creating SSL/TLS certificate with ACM
In the Using HTTPS with CloudFront domain recipe of Chapter 5, [Web Hosting with S3, Route53, and CloudFront](../../Chapter05/README.md), we used an SSL certificate. In this recipe, we will see how we can create such an SSL certificate using ACM. ACM is a free service for domains with a HostedZone in Route 53. ACM can also be used to import certificates created outside of AWS. 

## Getting ready
We should have a domain registered with AWS or with an outside registrar, and the domain should have a HostedZone available in Route 53. You may register a domain by following the Registering a domain with Route 53 recipe of Chapter 5, [Web Hosting with S3, Route53, and CloudFront](../../Chapter05/README.md).

## How to do it...
We will create certificates using both AWS CLI API commands and a CloudFormation template in this section. I have also included the steps for the AWS Management Console in the There's more section.

For certificates to work with CloudFront, AWS requires the certificates to be available in `us-east-1 region` and will get replicated to other required regions. 

### AWS CLI Commands
1. We can request a certificate from AWS Certificate Manager, passing the domain name and a validation method:
    ```bash
    aws acm request-certificate \
    --domain-name www.qnatime.net \
    --validation-method DNS \
    --profile admin
    ``` 
    Validation methods available at DNS and email. With DNS, we need to create a CNAME record in our domain's DNS records to verify the ownership of the domain. With email, we can verify using an email address. DNS is the preferred way, as per the AWS documentation.

    This command will return the ARN certificate:
    ```json
    ```

2. We can now use the `describe-certificate` subcommand to see the status, along with validation information:
    ```bash
    aws acm describe-certificate \
        --certificate-arn arn:aws:acm:us-east-1:218317422462:certificate/42b3ba99-66e9-4e71-8c1c-4239c1e81c84 \
        --profile admin
    ```    
    This command will return the validation status along with validation information:

    =
    The response also contains some additional information about the certificate, but will be incomplete at this point:
3. Create a change resource record set JSON for updating the CNAME record as required for DNS validation:
    ```json
    {
        "Comment": "change batch request for dns validation www.qnatime.net",
        "Changes": [
            {
            "Action": "CREATE",
            "ResourceRecordSet": {
                "Name": "_f086ad8e4c10e38385c3c36394a06182.www.qnatime.net.",
                "Type": "CNAME",
                "TTL": 300,
                "ResourceRecords": [
                {
                    "Value": "_ee9788f2dcf3eaefaa85bb096163ffd4.tljzshvwok.acm-validations.aws."
                }
                ]
            }
            }
        ]
    }
    ```
4. Execute the `change-resource-record-sets` subcommand of route53 CLI command:
    ```bash
    aws route53 change-resource-record-sets \
        --hosted-zone-id Z3G50MON7IDA18 \
        --change-batch file://resources/change-resource-record-sets-dns-validation.json \
        --profile admin
    ```    
    This command will return a change ID with the status as PENDING.
    We can then use the `get-change` subcommand of `route53` CLI command to check the status. The status should change to INSYNC when successful. 
5. Check the status of certificate creation using the `describe-certificate` subcommand. It might take some time before the validation is completed and the certificate is ready to use. If successful, we should get a response as follows:

    The initial part of the response contains a message about DNS validation success.

    Next, the response contains additional info about the certificate and issuer:


    The final part of the response contains `KeyUsages`, `ExtendedKeyUsages`, `RenewalEligibility`, and `Options`:

### CloudFormation Template
The certificate request process requires user interaction to verify the domain and hence it cannot be fully automated with CloudFormation scripts. However, I will still provide two templates to request for a certificate and verify the DNS. In the real world, you may just verify from the AWS Management Console or AWS CLI:
1. Use the following CloudFormation template for requesting a certificate from CloudFormation:
    ```yaml
    ---
    AWSTemplateFormatVersion: '2010-09-09'
    Description: 'Certificate Manager'
    Parameters:
    RootDomainName:
        Description: Domain name for generating certificate
        Type: String
    Resources:
    RootDomainCert:
        Type: AWS::CertificateManager::Certificate
        Properties:
        DomainName: !Ref RootDomainName
        ValidationMethod: DNS
    Outputs:
    CertificateArn:
        Value: !Ref RootDomainCert
    ```
2. Execute the template using the `create-stack` subcommand in `us-east-1 region` (the default).
    The stack will be created in the `CREATE_IN_PROGRESS` state, as we can verify with the describe-stacks subcommand. 

3. Use the describe-stack-events subcommand to get the CNAME values for DNS validation:
    ```bash
    aws cloudformation describe-stack-events \
        --stack-name cnamerecordsetstack \
        --profile admin
    ```    
    The CloudFormation stack with a resource of type `AWS::CertificateManager::Certificate` stays in the response state of `CREATE_IN_PROGRESS` until we verify the DNS with `CNAME`. `CNAME` is provided as an event during stack creation. If successful, the preceding command will return the list of events along with the details for the CNAME record in one of the event as shown here:



4. Add a `CNAME` record for DNS validation in the domain's `HostedZone`.
    You can use the `RecordSetGroup` resource to add a `CNAME` record in a new template file:
    ```yaml
    CNAMERecordSetGroup:
    Type: AWS::Route53::RecordSetGroup
    Properties:
        HostedZoneName: !Ref HostedZone
        Comment: Zone apex alias.
        RecordSets:
        -
        Name: !Ref CNAMEname
        Type: CNAME
        TTL: 900
        ResourceRecords:
            - !Ref CNAMEValue
    ```        
    Note that this is not a complete template. We also need to define three parameters, HostedZone, CNAMEname, and CNAMEValue, of type string. We can also define the template version and a description. The completed template is available in the code files.

5. After adding the `CNAME` record with the second stack, we can execute the `describe-stacks` subcommand against the first stack (certificate stack) and check the status until it is completed.

## How it works...
In summary, we did the following in this recipe:
1. Created a request for an SSL certificate
2. Verified that we own the domain through DNS updates and got the certificate issued

Most of the steps in the recipe are self-explanatory. To see the generated certificate in action, you may refer to the Using HTTPS with CloudFront domain within recipe of Chapter 5, Web Hosting with S3, Route53, and CloudFront.

## There's more...
We saw how to create SSL/TLS certificates using ACM. We can also import certificates created outside of AWS. We can use these certificates with services such as AWS Load Balancer, API Gateway API, and a CloudFront distribution. 

Apart from using SSL certificates and HTTPS, we can also add additional security to our web applications using services such as AWS WAF, AWS Shield, AWS Shield Advanced, and AWS Firewall Manager. 

You may also explore Let's Encrypt for creating free SSL/TLS certificates for your AWS deployments. Let's Encrypt is a certificate authority that provides free SSL certificates.

## See also
* To understand more about the use of a dedicated IP and SNI for serving HTTPS requests, you may refer to the following link: https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/cnames-https-dedicated-ip-or-sni.html
* Current ACM availability in different regions can be found at the following link: https://docs.aws.amazon.com/general/latest/gr/rande.html#acm_region