package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.renjin.eval.Context;
import org.renjin.packaging.LazyLoadFrame;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

public class MavenPackage extends Package {

  private String groupId;
  private String artifactId;

  public MavenPackage(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    LazyLoadFrame frame = new LazyLoadFrame(context, Resources.newInputStreamSupplier(getEnvironmentUrl()));
    List<NamedValue> values = Lists.newArrayList();
    for(Symbol name : frame.getNames()) {
      values.add(new PackageValue(name, frame.get(name)));
    }
    return values;
  }
  
  public URL getPomUrl() {
    return Resources.getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml");
  }
  
  public URL getNamespaceURL() {
    return Resources.getResource(groupId.replace('.', '/') + "/" + artifactId + "/NAMESPACE");
  }
  
  public URL getEnvironmentUrl() {
    String name = groupId.replace('.', '/') + "/" + artifactId + "/environment";
    return Resources.getResource(name);
  }
  
  @Override
  public NamespaceDef getNamespaceDef() {
    try {
      NamespaceDef def = new NamespaceDef();
      def.parse(Resources.newReaderSupplier(getNamespaceURL(), Charsets.UTF_8));
      return def;
    } catch(IOException e) {
      throw new RuntimeException("IOException while parsing NAMESPACE file");
    }
  }
  
  public boolean exists() {
    try {
      getEnvironmentUrl();
      return true;
    } catch(IllegalArgumentException e) {
      return false;
    }
  }
  
  private static class PackageValue implements NamedValue {
    private Symbol name;
    private SEXP value;
    
    public PackageValue(Symbol name, SEXP value) {
      super();
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean hasName() {
      return true;
    }

    @Override
    public String getName() {
      return name.getPrintName();
    }

    @Override
    public SEXP getValue() {
      return value;
    } 
  }

  @Override
  public InputSupplier<InputStream> getResource(String name) throws IOException {
    return Resources.newInputStreamSupplier(Resources.getResource((groupId + "/" + name).replace('.', '/')));
  }

  @Override
  public SEXP loadDataset(String datasetName) throws IOException {
    String resourceBase = "/" + groupId + "/" + artifactId + "/";

    Properties datasets = readDatasetIndex(resourceBase);
    String resourceName = datasets.getProperty(datasetName);
    if(Strings.isNullOrEmpty(resourceName)) {
      return Null.INSTANCE;
    } else {
      return readDataset(resourceBase + resourceName);
    }
  }

  private SEXP readDataset(String resourceName) throws IOException {
    InputStream in = getClass().getResourceAsStream(resourceName);
    if(in == null) {
      throw new IOException("Can't find resource " + resourceName);
    }
    if(resourceName.endsWith(".rda")) {
      GZIPInputStream gzin = new GZIPInputStream(in);
      try {
        RDataReader reader = new RDataReader(in);
        return reader.readFile();
      } finally {
        Closeables.closeQuietly(gzin);
      }
    } else {
      throw new UnsupportedOperationException("Don't know how to read " + resourceName);
    }
  }

  private Properties readDatasetIndex(String resourceBase) {
    Properties datasets = new Properties();
    InputStream in = getClass().getResourceAsStream(resourceBase + "datasets");
    if(in != null) {
      try {
        datasets.load(in);
        in.close();
      } catch(IOException e) {
      }
    }
    return datasets;
  }
  
  
}
