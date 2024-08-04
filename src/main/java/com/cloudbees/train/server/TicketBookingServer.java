package com.cloudbees.train.server;

import com.cloudbees.train.server.mapper.TicketMapper;
import com.cloudbees.train.server.persistence.TrainSeatManager;
import com.cloudbees.train.server.service.TicketManagerServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class TicketBookingServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(5003)
                .addService(new TicketManagerServiceImpl(new TrainSeatManager(), new TicketMapper()))
                .build();

        server.start();
        server.awaitTermination();
    }
}
