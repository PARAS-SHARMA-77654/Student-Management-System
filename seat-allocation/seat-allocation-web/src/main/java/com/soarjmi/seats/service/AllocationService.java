package com.soarjmi.seats.service;

import com.soarjmi.seats.model.AllocationResult;
import com.soarjmi.seats.model.Registration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class AllocationService {

    // Earliest registration first; name and email only break exact timestamp ties.
    private static final Comparator<Registration> FIRST_COME_FIRST_SERVED =
            Comparator.comparing(Registration::getTimestamp)
                    .thenComparing(Registration::getName)
                    .thenComparing(Registration::getEmail);

    public AllocationResult allocate(List<Registration> registrations, int seatCap) {
        // PriorityQueue is a binary min-heap, so the earliest timestamp is always at the head
        PriorityQueue<Registration> heap = new PriorityQueue<>(FIRST_COME_FIRST_SERVED);
        heap.addAll(registrations);

        List<Registration> confirmed = new ArrayList<>();
        List<Registration> waitlist = new ArrayList<>();

        while (!heap.isEmpty()) {
            Registration next = heap.poll();
            if (confirmed.size() < seatCap) {
                confirmed.add(next);
            } else {
                waitlist.add(next);
            }
        }
        return new AllocationResult(seatCap, confirmed, waitlist);
    }
}
