plugins {
    java
}

dependencies {
    implementation(project(":sample:zeta"))
    implementation(project(":sample:beta"))
    implementation(project(":sample:container:gama"))
    implementation(project(":sample:container:delta"))

    testImplementation(project(":sample:test"))
}
