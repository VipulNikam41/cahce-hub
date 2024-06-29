package com.lld;

import com.lld.cache.Cache;
import com.lld.cache.FlipCache;
import com.lld.config.Logger;
import com.lld.datastore.InMemoryDataSource;
import com.lld.eviction.TimeBoundEviction;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Cache<Integer, String> cache = new FlipCache<>(
                new TimeBoundEviction<>(5000L),
                new InMemoryDataSource<>()
        );

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.toUpperCase().startsWith("PUT") || input.toUpperCase().startsWith("P")) {
                String[] instructions = input.split(" ");
                cache.putValue(Integer.parseInt(instructions[1]), instructions[2]);
            } else if (input.toUpperCase().startsWith("GET") || input.toUpperCase().startsWith("G")) {
                String[] instructions = input.split(" ");
                Logger.info(cache.getValue(Integer.parseInt(instructions[1])));
            } else if (input.toUpperCase().startsWith("EXIT") || input.toUpperCase().startsWith("E")) {
                break;
            } else {
                Logger.error("Please give valid input");
            }
        }
    }
}