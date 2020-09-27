package io.etrace.collector.cluster.discovery;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;

public class DiscoveryExclude extends TypeExcludeFilter {

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
        throws IOException {
        //System.out.println(metadataReader.getClassMetadata());
        return super.match(metadataReader, metadataReaderFactory);
    }

    @Override
    public boolean equals(Object obj) {
        return false;

    }

    @Override
    public int hashCode() {
        return 1;
    }
}
