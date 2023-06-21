package com.zenyte.game.world.entity.player.login;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Kris | 17/05/2019 17:07
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 */
class AWS {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AWS.class);

    //Package constructor
    AWS() {
    }

    /**
     * Uploads a list of files async to the AWS storage.
     * @param files a list of files to upload, each of whom up to 5gb in size.
     */
    void upload(final List<File> files) {
        String clientRegion = "us-east-2";
        String bucketName = "zenyte-backup/server";
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIA3FNPO7OXXOAEBYUK", "IXVKhK1ZLCOg3dvdivGci+oM0ZDcAMWyupBhBs0b");
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
        final java.util.ArrayList<java.util.concurrent.Callable<java.lang.Void>> taskList = new ArrayList<Callable<Void>>();
        for (final java.io.File file : files) {
            taskList.add(() -> {
                try {
                    s3Client.putObject(new PutObjectRequest(bucketName, file.getName(), file));
                } catch (AmazonServiceException e) {
                    log.info("Call was transmitted successfully but AWS couldn\'t process it: ");
                    e.printStackTrace();
                } catch (SdkClientException e) {
                    log.info("AWS couldn\'t be contacted for a response: ");
                    e.printStackTrace();
                }
                return null;
            });
        }
        ForkJoinPool.commonPool().invokeAll(taskList);
    }
}
