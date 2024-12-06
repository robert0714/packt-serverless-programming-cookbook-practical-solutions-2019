package tech.heartin.books.serverlesscookbook.services;

import tech.heartin.books.serverlesscookbook.domain.Request;
import tech.heartin.books.serverlesscookbook.domain.Response;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

/**
 * Implementation class for KinesisService.
 */
public class KinesisServiceImpl implements KinesisService {

    private final KinesisClient  kinesisClient;
    private final List<PutRecordsRequestEntry> kinesisBatch;
    private static final String ERROR_MESSAGE = "Request completed with errors. Check Lambda logs for more details.";
    private static final String SUCCESS_MESSAGE = "Request completed without errors.";

    private boolean isError = false;
    private int documentAddedCount;


    public KinesisServiceImpl(final KinesisClient kinesisClient) {
        this.kinesisClient = kinesisClient;
        this.kinesisBatch = new ArrayList<>();
    }

    @Override
    public final Response addRecords(final Request request, final LambdaLogger logger) {

        this.documentAddedCount = request.getCount();

        DescribeStreamResponse result = this.kinesisClient.describeStream(
                DescribeStreamRequest.builder().streamName(request.getStreamName()).build()
        );
        logger.log("Stream Status: " + result.streamDescription().streamStatus() + ". ");

        logger.log("Adding records to Stream...");

        String payload;

        for (int i = 1; i <= request.getCount(); i++) {

            payload = request.getPayload() + i;

            this.kinesisBatch.add(PutRecordsRequestEntry.builder()
                    .partitionKey(request.getPartitionKey())
                    .data(SdkBytes.fromUtf8String(payload))
                    .build());

            if (this.kinesisBatch.size() >= request.getBatchSize()) {

                try {
                    logger.log("Flushing records to Stream...");
                    flushBatch(request.getStreamName(), logger);
                } catch (Exception e) {
                    logger.log("Exception occurred: " + e);
                    this.isError = false;
                } finally {
                    this.kinesisBatch.clear();
                }
            }

        }

        if (this.isError) {
            return new Response(ERROR_MESSAGE, documentAddedCount);
        } else {
            return new Response(SUCCESS_MESSAGE, documentAddedCount);
        }
    }

    private void flushBatch(final String streamName, final LambdaLogger logger) {
        final PutRecordsResponse  result = this.kinesisClient.putRecords(PutRecordsRequest.builder()
                .streamName(streamName)
                .records(this.kinesisBatch)
                .build());

        result.records().forEach(record -> {
            if  (record.errorCode() == null || record.errorCode().isEmpty()) {
                String successMessage = "Successfully processed record with sequence number: "
                        + record.sequenceNumber() + ", shard id: " + record.shardId();
                logger.log(successMessage);
            } else {
                this.documentAddedCount--;

                String errorMessage = "Did not process record with error code: " + record.errorCode()
                        + ", error message: " + record.errorMessage();
                logger.log(errorMessage);
                this.isError = true;
            }
        });


        // You may also implement a retry logic only for failed records (e.g. Create a list for failed records,
        // add error records to that list and finally retry all failed records until a max retry count is reached.)
        /*
        if (result.failedRecordCount() != null && result.failedRecordCount() > 0) {
            result.records().forEach(r -> {
                if ((r != null) && (StringUtils.isNotBlank(r.errorCode()))) {
                    // add this record to the retry list.
                }
            });
        }
        */
    }
}
