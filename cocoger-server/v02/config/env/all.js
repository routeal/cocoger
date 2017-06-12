'use strict';

module.exports = {
  app: {
    title: 'cocoger',
    description: 'cocoger service platform',
    keywords: ''
  },
  port: process.env.PORT || 3000,
  domain: process.env.ELB_DOMAIN || 'cocoger.com',
  templateEngine: 'swig',
  sessionSecret: 'MEAN',
  sessionCollection: 'sessions',
  jwtSecret: 'cocoger-2015',
  jwtIssuer: 'routeal.com',
  defaultTimezone: 'Asia/Tokyo',
  maxRequestCount: 3,
};
