-- Cleanup
DELETE FROM T_ORDER_ITEM;
DELETE FROM T_ORDER;
DELETE FROM T_CUSTOMER;
DELETE FROM T_ITEM;
DELETE FROM T_PRODUCT;
DELETE FROM T_CATEGORY;
DELETE FROM T_ACCOUNT;

-- Load
INSERT INTO T_ACCOUNT(userId,pwd) VALUES( 'user1', 'password1' );
INSERT INTO T_ACCOUNT(userId,pwd) VALUES( 'user2', 'password2' );
INSERT INTO T_ACCOUNT(userId,pwd) VALUES( 'user3', 'password3' );
INSERT INTO T_ACCOUNT(userId,pwd) VALUES( 'user4', 'password4' );
INSERT INTO T_ACCOUNT(userId,pwd) VALUES( 'user5', 'password5' );

INSERT INTO T_CUSTOMER(userId, firstname, lastname, email, telephone, language, street1, street2, city, state, zipcode, country, creditCardNumber, creditCardType, creditCardExpiryDate) VALUES( 'user1', 'firstname1', 'lastname1', 'herve@localdomain', '111-1111', 'en', 'street1.1', 'street1.2', 'city1', 'ST1', 'A1B-1C1', 'US', '111-111-111', 'Visa', '01-11' );
INSERT INTO T_CUSTOMER(userId, firstname, lastname, email, telephone, language, street1, street2, city, state, zipcode, country, creditCardNumber, creditCardType, creditCardExpiryDate) VALUES( 'user2', 'firstname2', 'lastname2', 'daniele@localdomain', '222-2222', 'en', 'street2.1', 'street2.2', 'city1', 'ST2', 'A2B-2C2', 'US', '222-222-222', 'Visa', '02-22' );

INSERT INTO T_CATEGORY(categoryId,name,description) VALUES ('FISH', 'Fish', 'description of FISH' );
INSERT INTO T_CATEGORY(categoryId,name,description) VALUES ('DOGS', 'Dogs', 'description of DOGS' );
INSERT INTO T_CATEGORY(categoryId,name,description) VALUES ('REPTILES', 'Reptiles', 'description of REPTILES' );
INSERT INTO T_CATEGORY(categoryId,name,description) VALUES ('CATS', 'Cats', 'description of CATS' );
INSERT INTO T_CATEGORY(categoryId,name,description) VALUES ('BIRDS', 'Birds', 'description of BIRDS' );

INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('FI-SW-01', 'Angelfish', 'Saltwater fish from Australia', 'FISH');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('FI-SW-02', 'Tiger Shark', 'Saltwater fish from Australia', 'FISH');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('FI-FW-01', 'Koi', 'Freshwater fish from Japan', 'FISH');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('FI-FW-02', 'Goldfish', 'Freshwater fish from China', 'FISH');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('K9-BD-01', 'Bulldog', 'Friendly dog from England', 'DOGS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('K9-PO-02', 'Poodle', 'Cute dog from France', 'DOGS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('K9-DL-01', 'Dalmation', 'Great dog for a fire station', 'DOGS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('K9-RT-01', 'Golden Retriever', 'Great family dog', 'DOGS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('K9-RT-02', 'Labrador Retriever', 'Great hunting dog', 'DOGS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('K9-CW-01', 'Chihuahua', 'Great companion dog', 'DOGS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('RP-SN-01', 'Rattlesnake', 'Doubles as a watch dog', 'REPTILES');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('RP-LI-02', 'Iguana', 'Friendly green friend', 'REPTILES');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('FL-DSH-01', 'Manx', 'Great for reducing mouse populations', 'CATS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('FL-DLH-02', 'Persian', 'Friendly house cat, doubles as a princess', 'CATS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('AV-CB-01', 'Amazon Parrot', 'Great companion for up to 75 years', 'BIRDS');
INSERT INTO T_PRODUCT(productId,name,description,category_fk)VALUES ('AV-SB-02', 'Finch', 'Great stress reliever', 'BIRDS');

INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-1', 'Large', 16.50, 10.00,  'fish1.jpg', 'FI-SW-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk)  VALUES ('EST-2', 'Thootless', 16.50, 10.00,  'fish1.jpg', 'FI-SW-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk)  VALUES ('EST-3', 'Spotted', 18.50, 12.00,  'fish2.jpg', 'FI-SW-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-4', 'Spotless', 18.50, 12.00,  'fish2.jpg', 'FI-SW-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-5', 'Male Adult', 18.50, 12.00,  'fish3.jpg', 'FI-FW-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-6', 'Female Adult', 18.50, 12.00,  'fish3.jpg', 'FI-FW-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-7', 'Male Puppy', 18.50, 12.00,  'fish4.jpg', 'FI-FW-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-8', 'Female Puppy', 18.50, 12.00,  'fish4.jpg', 'FI-FW-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-9', 'Spotless Male Puppy', 28.50, 22.00,  'dog1.jpg', 'K9-BD-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-10', 'Spotless Female Puppy', 28.50, 22.00,  'dog1.jpg', 'K9-BD-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-11', 'Spotted Male Puppy', 48.50, 32.00,  'dog2.jpg', 'K9-PO-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-12', 'Spotted Female Puppy', 58.50, 32.00,  'dog2.jpg', 'K9-PO-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-13', 'Tailed', 108.50, 62.00,  'dog3.jpg', 'K9-DL-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-14', 'Tailless', 108.50, 62.00,  'dog3.jpg', 'K9-DL-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-15', 'Tailed', 158.50, 82.00,  'dog4.jpg', 'K9-RT-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-16', 'Tailless', 158.50, 82.00,  'dog4.jpg', 'K9-RT-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-17', 'Tailed', 258.50, 100.00,  'dog5.jpg', 'K9-RT-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-18', 'Tailless', 258.50, 100.00,  'dog5.jpg', 'K9-RT-02');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-19', 'Female Adult', 208.50, 100.00,  'dog6.jpg', 'K9-CW-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-20', 'Female Adult', 208.50, 100.00,  'dog6.jpg', 'K9-CW-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-21', 'Female Adult', 48.50, 20.00,  'reptile1.jpg', 'RP-SN-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-22', 'Male Adult', 48.50, 20.00,  'reptile1.jpg', 'RP-SN-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-23', 'Male Adult', 348.50, 120.00,  'cat1.jpg', 'FL-DSH-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-24', 'Female Adult', 348.50, 120.00,  'cat1.jpg', 'FL-DSH-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-25', 'Male Adult', 348.50, 120.00,  'bird1.jpg', 'AV-CB-01');
INSERT INTO T_ITEM(itemId,description,listPrice,unitCost,imagePath,product_fk) VALUES ('EST-26', 'Female Adult', 348.50, 120.00,  'bird1.jpg', 'AV-CB-01');
