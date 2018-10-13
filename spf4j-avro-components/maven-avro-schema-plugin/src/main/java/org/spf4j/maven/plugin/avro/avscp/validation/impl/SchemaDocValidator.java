/*
 * Copyright 2018 SPF4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.maven.plugin.avro.avscp.validation.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.spf4j.avro.schema.SchemaVisitor;
import org.spf4j.avro.schema.SchemaVisitorAction;
import org.spf4j.maven.plugin.avro.avscp.validation.Validator;
import org.spf4j.avro.schema.Schemas;
import org.spf4j.base.CharSequences;

/**
 * @author Zoltan Farkas
 */
public class SchemaDocValidator implements Validator<Schema> {

  @Override
  public String getName() {
    return "docValidator";
  }

  @Override
  @Nonnull
  @SuppressFBWarnings("AI_ANNOTATION_ISSUES_NEEDS_NULLABLE") // not in this case
  public Result validate(final Schema schema) {
    return Schemas.visit(schema, new DocValidatorVisitor());
  }

  @Nullable
  private static Schema.Type getCollectionType(Schema unionSchema) {
    for (Schema schema : unionSchema.getTypes()) {
      Schema.Type type = schema.getType();
      switch (type) {
        case ARRAY:
        case MAP:
        case STRING:
          return type;
        default:
        // skip
      }
    }
    return null;
  }

  private static class DocValidatorVisitor implements SchemaVisitor<Result> {

    private final List<String> issues;

    public DocValidatorVisitor() {
      issues = new ArrayList<>(4);
    }

    @Override
    public SchemaVisitorAction visitTerminal(Schema schema) {
      switch (schema.getType()) {
        case ENUM:
        case FIXED:
          String doc = schema.getDoc();
          if (doc == null || doc.trim().isEmpty()) {
            issues.add("Please document " + schema.getFullName());
          }
          break;
        default:
      }
      return SchemaVisitorAction.CONTINUE;
    }

    @Override
    public SchemaVisitorAction visitNonTerminal(Schema schema) {
      if (schema.getType() == Schema.Type.RECORD) {
        String doc = schema.getDoc();
        if (doc == null || doc.trim().isEmpty()) {
          issues.add("Please document " + schema.getFullName());
        }
        for (Field field : schema.getFields()) {
          doc = field.doc();
          if (doc == null || doc.trim().isEmpty()) {
            issues.add("Please document " + field.name() + '@' + schema.getFullName());
          } else {
            Schema fs = field.schema();
            if (Schemas.isNullableUnion(fs) && !CharSequences.containsIgnoreCase(doc, "null")) {
              String issue = "please document the meaning of null for field " + field.name() + '@'
                      + schema.getFullName();
              Schema.Type collectionType = getCollectionType(fs);
              if (collectionType != null) {
                issue += " and explain how its meaning is different from empty " + collectionType;
              }
              issues.add(issue);
            }
          }

        }
      }
      return SchemaVisitorAction.CONTINUE;
    }

    @Override
    public SchemaVisitorAction afterVisitNonTerminal(Schema nonTerminal) {
      return SchemaVisitorAction.CONTINUE;
    }

    @Override
    @Nonnull
    public Result get() {
      return issues.isEmpty() ? Result.valid() :
              Result.failed("Schema Doc issues:\n" + String.join("\n", issues));
    }
  }

}