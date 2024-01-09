package com.yjy.nameservice;

import com.yjy.NameService;
import com.yjy.serialize.SerializeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 注册中心的实现类
 * 读写一个本地文件，
 * 实现注册服务 registerService 方法时，把服务提供者保存到本地文件中；
 * 实现查找服务 lookupService 时，就是去本地文件中读出所有的服务提供者，
 * 找到对应的服务提供者，然后返回
 */
public class LocalFileNameService implements NameService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileNameService.class);
    private static final Collection<String> schemes = Collections.singleton("file");
    private File file;

    /**
     * 返回支持的协议集合，通过定义常量，本例中只支持file协议
     */
    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    /**
     * 根据提供的URI连接到相应的服务，检查协议是否支持
     * @param nameServiceUri 注册中心地址
     */
    @Override
    public void connect(URI nameServiceUri) {
        if(schemes.contains(nameServiceUri.getScheme())){
            file = new File(nameServiceUri);
        }else {
            throw new RuntimeException("Unsupported scheme!");
        }
    }

    @Override
    public void registerService(String serviceName, URI uri) throws IOException {
        logger.info("Register service: {}, uri: {}.", serviceName, uri);
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = raf.getChannel()) {
            FileLock lock = fileChannel.lock();
            try {
                int fileLength = (int) raf.length();
                Metadata metadata;
                byte[] bytes;
                if(fileLength > 0) {
                    bytes = new byte[(int) raf.length()];
                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    while (buffer.hasRemaining()) {
                        fileChannel.read(buffer);
                    }

                    metadata = SerializeSupport.parse(bytes);
                } else {
                    metadata = new Metadata();
                }
                List<URI> uris = metadata.computeIfAbsent(serviceName, k -> new ArrayList<>());
                if(!uris.contains(uri)) {
                    uris.add(uri);
                }
                logger.info(metadata.toString());
                //调用序列化器完成序列化
                bytes = SerializeSupport.serialize(metadata);
                fileChannel.truncate(bytes.length);
                fileChannel.position(0L);
                fileChannel.write(ByteBuffer.wrap(bytes));
                fileChannel.force(true);
            } finally {
                lock.release();
            }
        }
    }

    @Override
    public URI lookupService(String serviceName) throws IOException {
        Metadata metadata;
        try(RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel fileChannel = raf.getChannel()) {
            FileLock lock = fileChannel.lock();
            try {
                //包装（wrap）,用file创建了一个channel，循环的读数据到bytes中
                byte [] bytes = new byte[(int) raf.length()];
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    fileChannel.read(buffer);
                }
                //文件中读到的内容反序列化位metadata
                metadata = bytes.length == 0? new Metadata(): SerializeSupport.parse(bytes);
                logger.info(metadata.toString());
            } finally {
                lock.release();
            }
        }
        //获取指定服务名的服务提供者列表
        List<URI> uris = metadata.get(serviceName);
        if(null == uris || uris.isEmpty()) {
            return null;
        } else {
            //随机选择一个并返回
            return uris.get(ThreadLocalRandom.current().nextInt(uris.size()));
        }
    }
}
