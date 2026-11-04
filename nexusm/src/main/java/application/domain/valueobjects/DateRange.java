package application.domain.valueobjects;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Date range value object.
 *
 * <p>Operations: containment, overlap and duration in days.</p>
 */
public final class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                "End date cannot be before start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static DateRange of(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    /**
     * Inclusive range that begins on {@code start} and lasts {@code days} days.
     */
    public static DateRange startingAt(LocalDate start, long days) {
        if (days < 1) {
            throw new IllegalArgumentException("Duration must be at least 1 day");
        }
        return new DateRange(start, start.plusDays(days - 1));
    }

    public boolean contains(LocalDate date) {
        return date != null && !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(DateRange other) {
        if (other == null) {
            throw new IllegalArgumentException("Range to compare cannot be null");
        }
        return !this.endDate.isBefore(other.startDate) && !other.endDate.isBefore(this.startDate);
    }

    /**
     * Number of days in the range (inclusive).
     */
    public long durationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DateRange dateRange = (DateRange) o;
        return startDate.equals(dateRange.startDate) && endDate.equals(dateRange.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "[" + startDate + ", " + endDate + "]";
    }
}