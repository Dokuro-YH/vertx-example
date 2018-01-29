create table "user" (
  username varchar(255) not null,
  password varchar(255) not null,
  password_salt varchar(255) not null,
  constraint pk_username primary key (username)
);

create table user_roles (
  username varchar(255) not null,
  role varchar(255) not null,
  constraint pk_user_roles primary key (username, role),
  constraint fk_username foreign key (username) references "user"(username)
);

create table roles_perms (
  role varchar(255) not null,
  perm varchar(255) not null,
  constraint pk_roles_perms primary key (role),
  constraint fk_roles foreign key (role) references roles_perms(role)
);