/**
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package io.jooby.maven;

import io.jooby.openapi.OpenAPIGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

@Mojo(name = "openapi", threadSafe = true,
    requiresDependencyResolution = COMPILE_PLUS_RUNTIME,
    aggregator = true,
    defaultPhase = PROCESS_CLASSES
)
public class OpenAPIMojo extends BaseMojo {

  @Parameter(defaultValue = "json,yaml")
  private String format;

  @Parameter(property = "openAPI.includes")
  private String includes;

  @Parameter(property = "openAPI.excludes")
  private String excludes;

  @Override protected void doExecute(List<MavenProject> projects, String mainClass)
      throws Exception {
    ClassLoader classLoader = createClassLoader(projects);

    getLog().info("Generating OpenAPI: " + mainClass);

    getLog().debug("Using classloader: " + classLoader);

    String[] names = mainClass.split("\\.");
    Path dir = Stream.of(names)
        .reduce(Paths.get(project.getBuild().getOutputDirectory()), Path::resolve, Path::resolve)
        .getParent();

    OpenAPIGenerator tool = new OpenAPIGenerator();
    tool.setClassLoader(classLoader);
    tool.setOutputDir(dir);
    trim(includes).ifPresent(tool::setIncludes);
    trim(excludes).ifPresent(tool::setExcludes);

    OpenAPI result = tool.generate(mainClass);

    for (OpenAPIGenerator.Format format : OpenAPIGenerator.Format.parse(this.format)) {
      tool.export(result, format);
    }
  }

  private Optional<String> trim(String value) {
    if (value == null || value.trim().length() == 0) {
      return Optional.empty();
    }
    return Optional.of(value.trim());
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getIncludes() {
    return includes;
  }

  public void setIncludes(String includes) {
    this.includes = includes;
  }

  public String getExcludes() {
    return excludes;
  }

  public void setExcludes(String excludes) {
    this.excludes = excludes;
  }
}