
package de.dropbox;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.io.Payload;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Container;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.options.CreateContainerOptions;
import org.jclouds.openstack.swift.v1.options.PutOptions;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import static com.google.common.io.ByteSource.wrap;
import static org.jclouds.io.Payloads.newByteSourcePayload;

public class JCloudsSwift implements Closeable {
   public static final String CONTAINER_NAME = "jclouds-example";
   public static final String OBJECT_NAME = "jclouds-example.txt";

   private SwiftApi swiftApi;

   public static void main(String[] args) throws IOException {
      JCloudsSwift jcloudsSwift = new JCloudsSwift();

      try {
         jcloudsSwift.createContainer();
         jcloudsSwift.uploadObjectFromString();
         jcloudsSwift.listContainers();
         jcloudsSwift.close();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         jcloudsSwift.close();
      }
   }

   public JCloudsSwift() {
      Iterable<Module> modules = ImmutableSet.<Module>of(
            new SLF4JLoggingModule());

      String provider = "openstack-swift";
      String identity = "demo:demo"; // tenantName:userName
      String credential = "credentials";

      swiftApi = ContextBuilder.newBuilder(provider)
    		.endpoint("http://8.43.86.2:5000/v2.0/")
            .credentials(identity, credential)
            .modules(modules)
            .buildApi(SwiftApi.class);
   }

   private void createContainer() {
      System.out.println("Create Container");

      ContainerApi containerApi = swiftApi.getContainerApi("RegionOne");
      CreateContainerOptions options = CreateContainerOptions.Builder
            .metadata(ImmutableMap.of(
                  "key1", "value1",
                  "key2", "value2"));

      containerApi.create(CONTAINER_NAME, options);

      System.out.println("  " + CONTAINER_NAME);
   }

   private void uploadObjectFromString() {
      System.out.println("Upload Object From String");

      ObjectApi objectApi = swiftApi.getObjectApi("RegionOne", CONTAINER_NAME);
      Payload payload = newByteSourcePayload(wrap("Hello World".getBytes()));

      objectApi.put(OBJECT_NAME, payload, PutOptions.Builder.metadata(ImmutableMap.of("key1", "value1")));

      System.out.println("  " + OBJECT_NAME);
   }

   private void listContainers() {
      System.out.println("List Containers");

      ContainerApi containerApi = swiftApi.getContainerApi("RegionOne");
      Set<Container> containers = containerApi.list().toSet();

      for (Container container : containers) {
         System.out.println("  " + container);
      }
   }

   public void close() throws IOException {
      Closeables.close(swiftApi, true);
   }
}
