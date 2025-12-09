package com.relax.reactor.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParameterParsingService {
    
    public List<Object> parseSequence(String sequenceStr) {
        if (!StringUtils.hasText(sequenceStr)) {
            throw new IllegalArgumentException("Sequence cannot be empty or null");
        }
        
        List<Object> sequence = new ArrayList<>();
        String[] parts = sequenceStr.split(",");
        
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            try {
                if (!part.contains(".")) {
                    sequence.add(Integer.parseInt(part));
                } else {
                    sequence.add(Double.parseDouble(part));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    String.format("Invalid sequence value: '%s'. Must be a valid number (integer or decimal).", part)
                );
            }
        }
        
        if (sequence.isEmpty()) {
            throw new IllegalArgumentException("Sequence must contain at least one valid number");
        }
        
        return sequence;
    }
    
    public List<Integer> parseStates(String statesStr) {
        if (!StringUtils.hasText(statesStr)) {
            return null;
        }
        
        List<Integer> states = new ArrayList<>();
        String[] parts = statesStr.split(",");
        
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            try {
                states.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    String.format("Invalid state value: '%s'. Must be a valid integer.", part)
                );
            }
        }
        
        return states.isEmpty() ? null : states;
    }
    
    public List<Integer> parseIntegerList(String input) {
        if (!StringUtils.hasText(input)) {
            return new ArrayList<>();
        }
        
        List<Integer> result = new ArrayList<>();
        String[] parts = input.split(",");
        
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            try {
                result.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    String.format("Invalid integer value: '%s'", part)
                );
            }
        }
        
        return result;
    }
    
    public List<Double> parseDoubleList(String input) {
        if (!StringUtils.hasText(input)) {
            return new ArrayList<>();
        }
        
        List<Double> result = new ArrayList<>();
        String[] parts = input.split(",");
        
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            try {
                result.add(Double.parseDouble(part));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    String.format("Invalid decimal value: '%s'", part)
                );
            }
        }
        
        return result;
    }
    
    public void validateSequenceSize(List<Object> sequence, int minSize) {
        if (sequence == null) {
            throw new IllegalArgumentException("Sequence cannot be null");
        }
        
        if (sequence.size() < minSize) {
            throw new IllegalArgumentException(
                String.format("Sequence must contain at least %d values, but only has %d", 
                    minSize, sequence.size())
            );
        }
    }
}