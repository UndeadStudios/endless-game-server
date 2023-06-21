package com.zenyte.api.aws

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.*
import com.zenyte.api.APIRequestResult
import com.zenyte.api.APIRequestRunnable
import com.zenyte.api.auth.AWSCredential
import com.zenyte.api.model.EmailStruct

/**
 *
 * @author Noele
 * see https://noeles.life || noele@zenyte.com
 */
class SendEmailSES(val email: EmailStruct) : APIRequestRunnable() {

    val AUTH = AWSCredential(System.getenv("SENDY_AWS_ACCESS"), System.getenv("SENDY_AWS_SECRET"))
    val FROM_EMAIL = "new@zenyte.com"

    val SUCCESS_RESULT = AmazonSESSendResult(true) to null
    val INVALID_RESULT = AmazonSESSendResult(false) to null

    override fun execute(): Pair<APIRequestResult, Exception?> {
        try {
            val auth = BasicAWSCredentials(AUTH.accessId, AUTH.secretKey)
            val client = AmazonSimpleEmailServiceClientBuilder.standard().
                    withRegion(Regions.US_EAST_1).
                    withCredentials(AWSStaticCredentialsProvider(auth)).build()
            val request = SendEmailRequest().
                    withDestination(Destination().withToAddresses(email.toEmail)).
                    withMessage(Message().withBody(
                            Body().withHtml(prepareData(email.htmlDigest)).
                                    withText(prepareData(email.textDigest))).
                            withSubject(prepareData(email.subject))).
                    withSource(FROM_EMAIL)

            client.sendEmail(request)
            // check if email send successfully
            return SUCCESS_RESULT
        } catch (e: Exception) {
            return INVALID_RESULT
        }

        return INVALID_RESULT
    }

    fun prepareData(data: String) : Content {
        return Content().withCharset("UTF-8").withData(data)
    }

    data class AmazonSESSendResult(override val result: Boolean) : APIRequestResult
}