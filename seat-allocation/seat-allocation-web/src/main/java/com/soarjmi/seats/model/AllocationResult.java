package com.soarjmi.seats.model;

import java.util.List;

public class AllocationResult {

    private final int seatCapacity;
    private final List<Registration> confirmed;
    private final List<Registration> waitlist;

    public AllocationResult(int seatCapacity, List<Registration> confirmed, List<Registration> waitlist) {
        this.seatCapacity = seatCapacity;
        this.confirmed = confirmed;
        this.waitlist = waitlist;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public List<Registration> getConfirmed() {
        return confirmed;
    }

    public List<Registration> getWaitlist() {
        return waitlist;
    }

    public int getTotal() {
        return confirmed.size() + waitlist.size();
    }

    public Registration getFirstConfirmed() {
        return confirmed.isEmpty() ? null : confirmed.get(0);
    }

    public Registration getFirstWaitlisted() {
        return waitlist.isEmpty() ? null : waitlist.get(0);
    }
}
