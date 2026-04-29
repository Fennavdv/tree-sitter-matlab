module lang::matlab::TypeSymbol

extend analysis::m3::TypeSymbol;

data TypeSymbol
    = \class(loc decl)
    | \method(loc decl)
    | \typeProperty(loc decl)
    | \typeArgument(loc decl)
    | \enumeration(loc decl)
    | \int8()
    | \uint8()
    | \int16()
    | \uint16()
    | \int32()
    | \uint32()
    | \int64()
    | \uint64()
    | \singe()
    | \double()
    | \logical()
    | \char()
    | \string()
    | \cell()
    | \struct()
    | \table()
    | \categorical()
    | \datetime()
    | \duration()
    ;