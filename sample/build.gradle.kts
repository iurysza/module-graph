task("check") {
    dependsOn(tasks.detekt)
    dependsOn(tasks.ktlintCheck)
}
