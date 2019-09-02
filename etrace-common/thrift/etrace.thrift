namespace java io.etrace.common.rpc

service MessageService{
    oneway void send(1:binary head,2:binary message);
}

