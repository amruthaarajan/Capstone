package com.amruthaa;


import java.util.concurrent.atomic.AtomicInteger;

public final class TicketIdGenerator {

    private static final AtomicInteger sequence = new AtomicInteger(1);

    public static int generate(){
        return sequence.getAndIncrement();
    }

}