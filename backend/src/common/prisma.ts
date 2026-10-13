/* eslint-disable @typescript-eslint/no-explicit-any */
let PrismaClientClass: any;

try {
  // تلاش برای بارگذاری کلاینت رسمی پریزما
  // eslint-disable-next-line @typescript-eslint/no-var-requires
  PrismaClientClass = require('@prisma/client').PrismaClient;
} catch {
  // پشتیبانی انعطاف‌پذیر در زمان تست‌های بدون دیتابیس (Mock Proxy)
  PrismaClientClass = class MockPrismaClient {
    constructor() {
      return new Proxy(this, {
        get: (_target, _prop) => {
          return new Proxy({}, {
            get: () => async () => null,
          });
        },
      });
    }
  };
}

declare global {
  // eslint-disable-next-line no-var
  var prisma: any | undefined;
}

export const prisma =
  global.prisma ||
  new PrismaClientClass({
    log: process.env.NODE_ENV === 'development' ? ['query', 'error', 'warn'] : ['error'],
  });

if (process.env.NODE_ENV !== 'production') {
  global.prisma = prisma;
}
