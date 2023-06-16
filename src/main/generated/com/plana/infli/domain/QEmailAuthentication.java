package com.plana.infli.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmailAuthentication is a Querydsl query type for EmailAuthentication
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailAuthentication extends EntityPathBase<EmailAuthentication> {

    private static final long serialVersionUID = 799171997L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmailAuthentication emailAuthentication = new QEmailAuthentication("emailAuthentication");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath certificateUrl = createString("certificateUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> expirationTime = createDateTime("expirationTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final QMember member;

    public final StringPath secret = createString("secret");

    public QEmailAuthentication(String variable) {
        this(EmailAuthentication.class, forVariable(variable), INITS);
    }

    public QEmailAuthentication(Path<? extends EmailAuthentication> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmailAuthentication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmailAuthentication(PathMetadata metadata, PathInits inits) {
        this(EmailAuthentication.class, metadata, inits);
    }

    public QEmailAuthentication(Class<? extends EmailAuthentication> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

