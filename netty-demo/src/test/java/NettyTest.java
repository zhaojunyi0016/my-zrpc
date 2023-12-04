import com.my.rpc.netty.AppClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author : Williams
 * Date : 2023/11/23 17:21
 */
public class NettyTest {

    @Test
    public void testByteBuf() {
        // 模拟请求头
        ByteBuf header = Unpooled.buffer();
        // 模拟请求体
        ByteBuf body = Unpooled.buffer();

        CompositeByteBuf byteBufs = Unpooled.compositeBuffer();
        // 通过逻辑组装, 而不是物理拷贝, 实现在 jvm 中的零拷贝
        byteBufs.addComponents(header, body);

    }

    @Test
    public void testWrapper() {
        byte[] buf1 = new byte[1024];
        byte[] buf2 = new byte[1024];
        // 共享byte数组的内容而不是拷贝，这也算零拷贝
        ByteBuf byteBur = Unpooled.wrappedBuffer(buf1, buf2);

    }


    /**
     * 封装报文
     *
     * @throws IOException
     */
    @Test
    public void testMessage() throws IOException {
        ByteBuf message = Unpooled.buffer();
        message.writeBytes("ydl".getBytes(StandardCharsets.UTF_8));
        message.writeByte(1);
        message.writeShort(125);
        message.writeInt(256);
        message.writeByte(1);
        message.writeByte(0);
        message.writeByte(2);
        message.writeLong(251455L);
        // 用对象流转化为字节数据
        AppClient appClient = new AppClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(appClient);
        byte[] bytes = outputStream.toByteArray();
        message.writeBytes(bytes);
        printAsBinary(message);
    }

    public static void printAsBinary(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(byteBuf.readerIndex(), bytes);
        String binaryString = ByteBufUtil.hexDump(bytes);
        StringBuilder formattedBinary = new StringBuilder();
        for (int i = 0; i < binaryString.length(); i += 2) {
            formattedBinary.append(binaryString.substring(i, i + 2)).append(" ");
        }
        System.out.println("Binary representation: " + formattedBinary.toString());
    }


    /**
     * 测试压缩
     */
    @Test
    public void testCompress() throws Exception {
        byte[] buffer = new byte[]{12, 3, 1, 1, 3, 13, 13, 13, 1, 32, 21, 22, 33, 14, 25, 26, 27};
        // 将 byte[] 作为输入, 将 结果输出到另外一个字节数组中
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos);
        gzipOutputStream.write(buffer);
        gzipOutputStream.finish();

        byte[] bytes = bos.toByteArray();
        System.out.println(bytes);
    }


    /**
     * 解压缩
     */
    @Test
    public void testDeCompress() throws Exception {
        byte[] buffer = new byte[]{12, 3, 1, 1, 3, 13, 13, 13, 1, 32, 21, 22, 33, 14, 25, 26, 27};

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        GZIPInputStream gzipInputStream = new GZIPInputStream(bais);
        int read = gzipInputStream.read();
        System.out.println(read);
    }



}
