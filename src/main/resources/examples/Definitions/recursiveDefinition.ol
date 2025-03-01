service Main {

  outputPort output{
        oneWay:
            f1(any)
    }

    define definition{
        f1@output(void)
    }

    define recursiveDefinition{
        definition
        f1@output(void)
    }

    main {
        recursiveDefinition
    }
}
