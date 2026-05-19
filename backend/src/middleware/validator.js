const { validationResult } = require('express-validator');
const { AppError } = require('./errorHandler');

const validate = (req, res, next) => {
  const errors = validationResult(req);
  
  if (!errors.isEmpty()) {
    const errorMessages = errors.array().map(err => ({
      field: err.path,
      message: err.msg
    }));

    const error = new AppError(errorMessages[0]?.message || 'Validation failed', 400);
    error.errors = errorMessages;
    return next(error);
  }
  
  next();
};

module.exports = validate;
