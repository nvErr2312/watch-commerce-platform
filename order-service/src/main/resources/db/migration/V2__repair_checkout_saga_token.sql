-- AxonServer was reset while the local H2 token store was kept. The stale
-- CheckoutSaga token can therefore point beyond the current event stream.
update token_entry
set token = (
        select projection.token
        from token_entry projection
        where projection.processor_name = 'order-projection'
          and projection.segment = 0
    ),
    token_type = (
        select projection.token_type
        from token_entry projection
        where projection.processor_name = 'order-projection'
          and projection.segment = 0
    ),
    owner = null
where processor_name = 'CheckoutSagaProcessor'
  and segment = 0
  and exists (
      select 1
      from token_entry projection
      where projection.processor_name = 'order-projection'
        and projection.segment = 0
  );
