package com.clearledger.income_service.service;

import com.clearledger.income_service.dto.*;
import com.clearledger.income_service.entity.IncomeEntry;
import com.clearledger.income_service.entity.IncomeStream;
import com.clearledger.income_service.repository.IncomeEntryRepository;
import com.clearledger.income_service.repository.IncomeStreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeStreamRepository streamRepository;
    private final IncomeEntryRepository entryRepository;

    // ── Streams ──────────────────────────────────────────────────────────────

    @Transactional
    public IncomeStreamResponse createStream(String userId, CreateStreamRequest req) {
        IncomeStream stream = IncomeStream.builder()
                .userId(userId)
                .name(req.getName())
                .description(req.getDescription())
                .build();
        stream = streamRepository.save(stream);
        return toStreamResponse(stream);
    }

    public List<IncomeStreamResponse> getStreams(String userId) {
        return streamRepository.findByUserId(userId)
                .stream()
                .map(this::toStreamResponse)
                .toList();
    }

    // ── Entries ──────────────────────────────────────────────────────────────

    @Transactional
    public IncomeEntryResponse createEntry(String userId, CreateEntryRequest req) {

        // Verify the stream belongs to this user before adding an entry
        IncomeStream stream = streamRepository
                .findByIdAndUserId(req.getStreamId(), userId)
                .orElseThrow(() -> new RuntimeException("Stream not found or access denied"));

        IncomeEntry entry = IncomeEntry.builder()
                .stream(stream)
                .userId(userId)
                .amount(req.getAmount())
                .description(req.getDescription())
                .earnedOn(req.getEarnedOn())
                .build();

        entry = entryRepository.save(entry);
        return toEntryResponse(entry);
    }

    public List<IncomeEntryResponse> getEntries(String userId, String streamId) {
        List<IncomeEntry> entries = (streamId != null)
                ? entryRepository.findByStream_IdAndUserId(streamId, userId)
                : entryRepository.findByUserIdOrderByEarnedOnDesc(userId);

        return entries.stream().map(this::toEntryResponse).toList();
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    public IncomeSummaryResponse getSummary(String userId, int month, int year) {

        List<Object[]> rows = entryRepository.findMonthlySummaryByUser(userId, month, year);

        List<IncomeSummaryResponse.StreamSummary> byStream = rows.stream()
                .map(row -> IncomeSummaryResponse.StreamSummary.builder()
                        .streamId((String) row[0])
                        .streamName((String) row[1])
                        .total((BigDecimal) row[2])
                        .entryCount((Long) row[3])
                        .build())
                .toList();

        BigDecimal total = byStream.stream()
                .map(IncomeSummaryResponse.StreamSummary::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return IncomeSummaryResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(total)
                .byStream(byStream)
                .build();
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private IncomeStreamResponse toStreamResponse(IncomeStream s) {
        BigDecimal total = (s.getEntries() == null) ? BigDecimal.ZERO :
                s.getEntries().stream()
                        .map(IncomeEntry::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return IncomeStreamResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .totalEarned(total)
                .entryCount(s.getEntries() == null ? 0 : s.getEntries().size())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private IncomeEntryResponse toEntryResponse(IncomeEntry e) {
        return IncomeEntryResponse.builder()
                .id(e.getId())
                .streamId(e.getStream().getId())
                .streamName(e.getStream().getName())
                .amount(e.getAmount())
                .description(e.getDescription())
                .earnedOn(e.getEarnedOn())
                .build();
    }
}