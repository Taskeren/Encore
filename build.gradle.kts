/*
 * Copyright (c) 2024 Taskeren and Contributors - All Rights Reserved.
 */

@file:Suppress("SpellCheckingInspection")

plugins {
	java
	id("xyz.jpenilla.run-paper") version "2.2.2"
	id("io.papermc.paperweight.userdev") version "1.5.10"
}

group = "cn.taskeren"
version = "1.0.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven("https://papermc.io/repo/repository/maven-public/")
	maven("https://oss.sonatype.org/content/groups/public/")
	maven("https://repo.dmulloy2.net/repository/public/")
}

val paper_version: String by project
val protocol_lib_version: String by project
val minecraft_version: String by project

dependencies {
	paperweight.paperDevBundle(paper_version)
	compileOnly("com.comphenix.protocol:ProtocolLib:${protocol_lib_version}")
}

val targetJavaVersion = 17
java {
	val javaVersion = JavaVersion.toVersion(targetJavaVersion)
	sourceCompatibility = javaVersion
	targetCompatibility = javaVersion
	if(JavaVersion.current() < javaVersion) {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
		}
	}
}

tasks.withType<JavaCompile> {
	if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
		options.release.set(targetJavaVersion)
	}
}

tasks {
	processResources {
		val props = mapOf(
			"version" to version
		)
		inputs.properties(props)
		filteringCharset = "UTF-8"
		filesMatching("plugin.yml") {
			expand(props)
		}
	}

	assemble {
		dependsOn("reobfJar")
	}

	runServer {
		minecraftVersion(minecraft_version)

		// add debug agentlib
		jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")

		downloadPlugins {
			hangar("ProtocolLib", protocol_lib_version)
		}
	}
}
