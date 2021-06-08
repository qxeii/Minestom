package net.minestom.codegen.potion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.squareup.javapoet.*;
import net.minestom.codegen.MinestomCodeGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.*;
import java.util.Collections;

public final class PotionEffectGenerator extends MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(PotionEffectGenerator.class);
    private final InputStream potionEffectsFile;
    private final File outputFolder;

    public PotionEffectGenerator(@Nullable InputStream potionEffectsFile, @NotNull File outputFolder) {
        this.potionEffectsFile = potionEffectsFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (potionEffectsFile == null) {
            LOGGER.error("Failed to find potionEffects.json.");
            LOGGER.error("Stopped code generation for potion effects.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }
        // Important classes we use alot
        ClassName namespaceIDClassName = ClassName.get("net.minestom.server.utils", "NamespaceID");
        ClassName registriesClassName = ClassName.get("net.minestom.server.registry", "Registries");

        JsonArray potionEffects = GSON.fromJson(new JsonReader(new InputStreamReader(potionEffectsFile)), JsonArray.class);
        ClassName potionEffectClassName = ClassName.get("net.minestom.server.potion", "PotionEffect");

        // Particle
        TypeSpec.Builder potionEffectClass = TypeSpec.enumBuilder(potionEffectClassName)
                .addSuperinterface(ClassName.get("net.kyori.adventure.key", "Keyed"))
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());

        potionEffectClass.addField(
                FieldSpec.builder(namespaceIDClassName, "id")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).addAnnotation(NotNull.class).build()
        );
        // static field
        potionEffectClass.addField(
                FieldSpec.builder(ArrayTypeName.of(potionEffectClassName), "VALUES")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("values()")
                        .build()
        );

        potionEffectClass.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(namespaceIDClassName, "id").addAnnotation(NotNull.class).build())
                        .addStatement("this.id = id")
                        .addStatement("$T.potionEffects.put(id, this)", registriesClassName)
                        .build()
        );
        // Override key method (adventure)
        potionEffectClass.addMethod(
                MethodSpec.methodBuilder("key")
                        .returns(ClassName.get("net.kyori.adventure.key", "Key"))
                        .addAnnotation(Override.class)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getId method
        potionEffectClass.addMethod(
                MethodSpec.methodBuilder("getId")
                        .returns(TypeName.SHORT)
                        .addStatement("return (short) (ordinal() + 1)")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getNamespaceID method
        potionEffectClass.addMethod(
                MethodSpec.methodBuilder("getNamespaceID")
                        .returns(namespaceIDClassName)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // fromId Method
        potionEffectClass.addMethod(
                MethodSpec.methodBuilder("fromId")
                        .returns(potionEffectClassName)
                        .addAnnotation(Nullable.class)
                        .addParameter(TypeName.SHORT, "id")
                        .beginControlFlow("if(id >= 1 && id < VALUES.length + 1)")
                        .addStatement("return VALUES[id - 1]")
                        .endControlFlow()
                        .addStatement("return null")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        // toString method
        potionEffectClass.addMethod(
                MethodSpec.methodBuilder("toString")
                        .addAnnotation(NotNull.class)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        // this resolves to [Namespace]
                        .addStatement("return \"[\" + this.id + \"]\"")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );

        // Use data
        for (JsonElement pe : potionEffects) {
            JsonObject potionEffect = pe.getAsJsonObject();

            String potionEffectName = potionEffect.get("name").getAsString();

            potionEffectClass.addEnumConstant(potionEffectName, TypeSpec.anonymousClassBuilder(
                    "$T.from($S)",
                    namespaceIDClassName,
                    potionEffect.get("id").getAsString()
                    ).build()
            );
        }

        // Write files to outputFolder
        writeFiles(
                Collections.singletonList(
                        JavaFile.builder("net.minestom.server.potion", potionEffectClass.build())
                                .indent("    ")
                                .skipJavaLangImports(true)
                                .build()
                ),
                outputFolder
        );
    }
}
