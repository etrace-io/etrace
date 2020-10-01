package io.etrace.stream.container.exporter.kafka.model;

public class TopicSeq {
    public int seq;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TopicSeq seq1 = (TopicSeq)o;

        return seq == seq1.seq;
    }

    @Override
    public int hashCode() {
        return seq;
    }

}
