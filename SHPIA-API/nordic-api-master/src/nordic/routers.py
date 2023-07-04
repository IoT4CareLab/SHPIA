import core.models

allmodels = dict([(name.lower(), cls) for name, cls in core.models.__dict__.items() if isinstance(cls, type)])

class HybridRouter:

    def allow_migrate(self, db, app_label, model_name = None, **hints):
        """ migrate to appropriate database per model """

        try:
            model = allmodels.get(model_name)
            return model.params.db == db
        except Exception as e:
            return None