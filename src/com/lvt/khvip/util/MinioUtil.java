package com.lvt.khvip.util;

import java.io.ByteArrayInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;

public class MinioUtil {
	private static final Logger log = LoggerFactory.getLogger(MinioUtil.class);
	public static final MinioClient minioClient;

	static {
		String endpoint = ConfProperties.getProperty("minio.url");
		minioClient = MinioClient.builder() //
				.endpoint(endpoint) //
				.credentials(ConfProperties.getProperty("minio.accessKey"), ConfProperties.getProperty("minio.secretKey")) //
				.build();
		log.info("// initialized minioClient - endpoint = " + endpoint);
	}
	
	public static void uploadImage(String fileName, byte[] data) throws Exception {
		String bucketName = ConfProperties.getProperty("minio.bucketName");
		String contentType = ConfProperties.getProperty("minio.contentType");
		ByteArrayInputStream bs = new ByteArrayInputStream(data);
		ObjectWriteResponse res = minioClient.putObject(
				PutObjectArgs.builder().bucket(bucketName)
						.object(fileName).stream(bs, data.length, -1)
						.contentType(contentType)
						.build());
		log.info("Filename={}. Etag {} saved to {}", fileName, res.etag(), res.object());
	}
}
