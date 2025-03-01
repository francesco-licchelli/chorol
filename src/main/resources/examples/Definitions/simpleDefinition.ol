service Main {

  outputPort output{
        oneWay:
            ow(any)
    }

    define definition{
        ow@output(void)
    }

    main {
        definition
    }
}
