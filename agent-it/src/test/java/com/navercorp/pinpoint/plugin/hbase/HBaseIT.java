package com.navercorp.pinpoint.plugin.hbase;

import com.chinamobile.cmss.pinpoint.hbase.PinPointTraceInfoProtos;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.RPCProtos;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({ "org.apache.hbase:hbase-client:1.2.6.1"})
public class HBaseIT {

    private final static Object lock = new Object();
    private static volatile Connection conn;
    private static final String TABLE_NAME = "test";

    @Test
    public void testHTableGet() throws Exception{
        Table table = getTable();
        Get get = new Get("1".getBytes());
        Result result = table.get(get);
        System.out.println("HBase result " + result.toString());
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
    }

    @Test
    public void testHTablePut() throws Exception{
        Table table = getTable();
        Put put = new Put("1".getBytes());
        put.addColumn("f".getBytes(),"age".getBytes(),"18".getBytes());
        table.put(put);
    }


    public void testRespondHeader() throws IOException {
        RPCProtos.RequestHeader.Builder builder = RPCProtos.RequestHeader.newBuilder();
        builder.setCallId(11);
        builder.setMethodName("testRespondHeader");
        builder.setRequestParam(false);
        RPCProtos.RequestHeader header = builder.build();

        PinPointTraceInfoProtos.PinPointTraceInfo.Builder traceInfoBuilder = PinPointTraceInfoProtos.PinPointTraceInfo.newBuilder();
        traceInfoBuilder.setPinpointSampled(true);
        traceInfoBuilder.setPinpointTraceId("agent 0001");
        traceInfoBuilder.setPinpointSpanId(1001);
        traceInfoBuilder.setPinpointParentSpanId(1000);
        traceInfoBuilder.setPinpointFlags(0);
        traceInfoBuilder.setPinpointParentApplicationName("HbaseIT");
        traceInfoBuilder.setPinpointParentApplicationType(9019);
        traceInfoBuilder.setPinpointHost("mac-local");
        PinPointTraceInfoProtos.PinPointTraceInfo traceInfo = traceInfoBuilder.build();

        int msgTotalSize = header.getSerializedSize() + traceInfo.getSerializedSize();
        byte buf[] = new byte[msgTotalSize];
        CodedOutputStream outputStream =  CodedOutputStream.newInstance(buf);
        header.writeTo(outputStream);
        traceInfo.writeTo(outputStream);
        int spaceLeft = outputStream.spaceLeft();
        outputStream.flush();

        Message.Builder rebuilder = RPCProtos.RequestHeader.newBuilder();
        ProtobufUtil.mergeFrom(rebuilder, buf, 0, msgTotalSize - spaceLeft);
        RPCProtos.RequestHeader reheader = (RPCProtos.RequestHeader) rebuilder.build();
        UnknownFieldSet unknownFieldSet = reheader.getUnknownFields();

        PinPointTraceInfoProtos.PinPointTraceInfo.Builder recoveryTraceBuilder = PinPointTraceInfoProtos.PinPointTraceInfo.newBuilder();
        buf = new byte[unknownFieldSet.getSerializedSize()];
        outputStream =  CodedOutputStream.newInstance(buf);
        unknownFieldSet.writeTo(outputStream);
        ProtobufUtil.mergeFrom(recoveryTraceBuilder, buf, 0, unknownFieldSet.getSerializedSize());
        final PinPointTraceInfoProtos.PinPointTraceInfo reTraceInfo = recoveryTraceBuilder.build();
        System.out.println(reTraceInfo.toString());
    }

    public Table getTable() throws IOException {
        TableName tableName = TableName.valueOf(TABLE_NAME);
        Table table = getConnection().getTable(tableName);
        return table;
    }

    public static Connection getConnection() throws IOException {
        if (conn == null) {
            synchronized (lock) {
                if (conn == null) {
                    Configuration hbaseConf = HBaseConfiguration.create();
                    hbaseConf.set("hbase.zookeeper.quorum", "docker1");
                    hbaseConf.set("hbase.zookeeper.property.clientPort", "2181");
                    hbaseConf.set("zookeeper.znode.parent", "/hbase-unsecure");
                    conn = ConnectionFactory.createConnection(hbaseConf);
                }
            }
        }
        return conn;
    }

    /**
     * int转byte数组
     * @param
     * @return
     */
    public byte[]IntToByte(int num){
        byte[]bytes=new byte[4];
        bytes[0]=(byte) ((num>>24)&0xff);
        bytes[1]=(byte) ((num>>16)&0xff);
        bytes[2]=(byte) ((num>>8)&0xff);
        bytes[3]=(byte) (num&0xff);
        return bytes;
    }

}
