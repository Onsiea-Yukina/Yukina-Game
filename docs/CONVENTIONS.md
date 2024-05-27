
        # Coding Conventions

## General Conventions

- All code should be properly commented using Javadoc for methods and classes.
- Follow the naming conventions as outlined below:
  - Use camelCase for variables and methods.
  - Use PascalCase for class names.
  - Constants should be in UPPER_SNAKE_CASE.

## Specific Conventions

- Use the suffix `In` for parameters.
- Always use `this.` when accessing class variables or non-static methods.
- Do not use the prefixes `get`, `set`, `is` for methods.
- Place braces on the next line.
- Use Lombok annotations where applicable to reduce boilerplate code.

## File and Directory Structure

- Maintain a clear and organized file structure as shown below:
  - `src/main/java` for main source code.
  - `src/main/resources` for resource files.
  - `src/test/java` for test code.
  - `src/test/resources` for test resource files.

## Libraries and Dependencies

- Use Maven for managing dependencies.
- Ensure that all libraries are up-to-date and properly documented in the `pom.xml` file.

## Error Handling

- Provide detailed error messages and log them using SLF4J.
- Always handle exceptions gracefully and provide meaningful feedback to the user.
        