/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

ALTER TABLE process DROP CONSTRAINT IF EXISTS reject_duplicate_process_no_parameters;
ALTER TABLE process DROP CONSTRAINT IF EXISTS reject_duplicate_process_with_parameters;

ALTER TABLE process ADD CONSTRAINT reject_duplicate_process_no_parameters EXCLUDE USING gist (
  script WITH =,
  TSRANGE(creation_time - INTERVAL '${half_window} ms',
          creation_time + INTERVAL '${half_window} ms') WITH &&
) WHERE (
  parameters IS NULL
    AND creation_time > '${after}'  -- only apply the constraint to newly created Processes
);

ALTER TABLE process ADD CONSTRAINT reject_duplicate_process_with_parameters EXCLUDE USING gist (
  script WITH =,
  parameters WITH =,
  TSRANGE(creation_time - INTERVAL '${half_window} ms',
          creation_time + INTERVAL '${half_window} ms') WITH &&
) WHERE (
  parameters IS NOT NULL
    AND creation_time > '${after}' -- only apply the constraint to newly created Processes
);
