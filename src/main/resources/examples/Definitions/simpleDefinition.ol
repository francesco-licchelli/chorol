interface ExampleInt {
    oneWay:
        ow(any)
    requestResponse:
        rr(any)(any)
}

service Main {

  outputPort output{
        location: "socket://localhost:0000"
        interfaces: ExampleInt
    }

    define definition{
        ow@output(void)
    }

    main {
        rr@output(void)(void)
        definition
    }
}
