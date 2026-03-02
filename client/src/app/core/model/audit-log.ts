class AuditAction {
  code!: string;
  name!: string;
}

export class AuditLog {
  actionDate!: Date;
  auditAction!: AuditAction;
  //user!: User;
  //role!: Role;
  //auditUser!: User;
  //auditRole!: Role;
}
