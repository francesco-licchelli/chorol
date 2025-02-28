interface ExampleInt {
    oneWay:
        f1(any)
}

service Main {

  outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }

    define definition{
        f1@output(void)
    }

    define metadefinition{
        definition
        f1@output(void)
    }

    main {
        metadefinition
    }
}
